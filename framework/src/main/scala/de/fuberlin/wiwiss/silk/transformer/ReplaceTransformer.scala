package de.fuberlin.wiwiss.silk.transformer

import de.fuberlin.wiwiss.silk.linkspec.Transformer


class ReplaceTransformer(val params: Map[String, String] = Map()) extends Transformer
{
    require(params.contains("search"), "Parameter 'search' is required")
    require(params.contains("replace"), "Parameter 'replace' is required")

    val search = params("search")
    val replace = params("replace")

    override def evaluate(strings : Seq[String]) =
    {
        if (strings.size > 1) throw new IllegalArgumentException("Only one value allowed for replace transform function.")
        strings.toList.head.replace(search, replace)
    }
}