package de.fuberlin.wiwiss.silk.learning.generation

import de.fuberlin.wiwiss.silk.util.SourceTargetPair
import de.fuberlin.wiwiss.silk.learning.individual.FunctionNode
import de.fuberlin.wiwiss.silk.linkspec.similarity.DistanceMeasure
import de.fuberlin.wiwiss.silk.learning.LearningConfiguration.Components
import de.fuberlin.wiwiss.silk.evaluation.ReferenceEntities
import de.fuberlin.wiwiss.silk.entity.{Entity, Path}

/**
 * Analyses the reference entities and generates pairs of paths.
 */
class PathPairGenerator(components: Components) {
  def apply(entities: ReferenceEntities): Traversable[ComparisonGenerator] = {
    if(entities.positive.isEmpty || entities.negative.isEmpty) {
      Traversable.empty
    } else {
      //Get all paths except sameAs paths
      val paths = PathsRetriever(entities)
      //Remove paths which hold the same values (e.g. rdfs:label and drugbank:drugName)
      val distinctPaths = DuplicateRemover(paths, entities)
      //Return all path pairs
      val pathPairs = PairGenerator(distinctPaths, entities)

      //pathPairs.foreach(p => printLink(p, instances))
      //pathPairs.foreach(println)

      pathPairs.flatMap(createGenerators)
    }
  }

//TODO remove
//  private def printLink(pathPair: SourceTargetPair[Path], instances: ReferenceInstances) {
//    println("-------------------------------------")
//    println(pathPair.mkString(" - "))
//    println("P")
//    for(instancePair <- instances.positive.values) {
//      println(instancePair.source.evaluate(pathPair.source).mkString(", ") + "\n---\n" + instancePair.target.evaluate(pathPair.target).mkString(", "))
//    }
//    println("N")
//    for(instancePair <- instances.negative.values) {
//      println(instancePair.source.evaluate(pathPair.source).mkString(", ") + "\n---\n" + instancePair.target.evaluate(pathPair.target).mkString(", "))
//    }
//  }

  private def createGenerators(pathPair: SourceTargetPair[Path]) = {
    new ComparisonGenerator(InputGenerator.fromPathPair(pathPair, components.transformations), FunctionNode("levenshteinDistance", Nil, DistanceMeasure), 5.0) ::
    new ComparisonGenerator(InputGenerator.fromPathPair(pathPair, components.transformations), FunctionNode("jaccard", Nil, DistanceMeasure), 1.0) :: Nil
  }

  /**
   * Retrieves all paths except sameAs paths
   */
  private object PathsRetriever {
    def apply(entities: ReferenceEntities) = {
      val allPaths = entities.positive.values.head.map(_.desc.paths)
      allPaths.map(_.filterNot(_.toString.contains("sameAs")))
    }
  }

  /**
   * Removes paths which hold the same values (e.g. rdfs:label and drugbank:drugName)
   */
  private object DuplicateRemover {
    def apply(paths: SourceTargetPair[Traversable[Path]], entities: ReferenceEntities) = {
      SourceTargetPair(
        source = removeDuplicatePaths(entities.positive.values.map(_.source) ++ entities.negative.values.map(_.source), paths.source),
        target = removeDuplicatePaths(entities.positive.values.map(_.target) ++ entities.negative.values.map(_.target), paths.target)
      )
    }

    private def removeDuplicatePaths(entities: Traversable[Entity], paths: Traversable[Path]): Traversable[Path] = {
      for(path :: tail <- paths.toList.tails.toTraversable
          if !tail.exists(p => pathsMatch(entities, SourceTargetPair(path, p)))) yield path
    }

    private def pathsMatch(entities: Traversable[Entity], pathPair: SourceTargetPair[Path]): Boolean = {
      entities.forall(pathsMatch(_, pathPair))
    }

    private def pathsMatch(entities: Entity, pathPair: SourceTargetPair[Path]): Boolean = {
      val values = pathPair.map(entities.evaluate)

      val valuePairs = for(v1 <- values.source; v2 <- values.target) yield SourceTargetPair(v1, v2)

      valuePairs.exists(p => p.source.toLowerCase == p.target.toLowerCase)
    }
  }

  /**
   * Generates all pair of paths with overlapping values.
   */
  private object PairGenerator {
    def apply(paths: SourceTargetPair[Traversable[Path]], entities: ReferenceEntities) = {
      val pathPairs = for(sourcePath <- paths.source; targetPath <- paths.target) yield SourceTargetPair(sourcePath, targetPath)

      pathPairs.filter(pathValuesMatch(entities, _))
    }

    private def pathValuesMatch(instances: ReferenceEntities, pathPair: SourceTargetPair[Path]): Boolean = {
      val positiveMatches = instances.positive.values.filter(i => matches(i, pathPair))
      val negativeMatches = instances.negative.values.filter(i => matches(i, pathPair))

      val positive = positiveMatches.size.toDouble / instances.positive.size
      val negative = negativeMatches.size.toDouble / instances.negative.size

      positive > negative
    }

    private def matches(entityPair: SourceTargetPair[Entity], pathPair: SourceTargetPair[Path]): Boolean = {
      val sourceValues = entityPair.source.evaluate(pathPair.source)
      val targetValues = entityPair.target.evaluate(pathPair.target)

      val valuePairs = for(sourceValue <- sourceValues; targetValue <- targetValues) yield valuesMatch(sourceValue, targetValue)

      valuePairs.exists(identity)
    }

    private def valuesMatch(value1: String, value2: String): Boolean = {
      val tokens1 = value1.toLowerCase.split("\\s")
      val tokens2 = value2.toLowerCase.split("\\s")

      val valuePairs = for(t1 <- tokens1; t2 <- tokens2) yield t1 == t2

      valuePairs.exists(identity)
    }
  }
}