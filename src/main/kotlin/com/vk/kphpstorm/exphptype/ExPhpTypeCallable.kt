package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * Typed callable, e.g. 'callable(int, float):int'
 * Simple phpdoc "callable" is NOT this! It is primitive.
 */
class ExPhpTypeCallable(val argTypes: List<ExPhpType>, val returnType: ExPhpType?) : ExPhpType {
    override fun toString() = "callable(${argTypes.joinToString(",")}):${returnType ?: "void"}"

    override fun toHumanReadable(expr: PhpPsiElement) = "callable(${argTypes.joinToString { it.toHumanReadable(expr) }}):${returnType?.toHumanReadable(expr) ?: "void"}"

    override fun toPhpType(): PhpType {
        return PhpType.CALLABLE
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return null
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return ExPhpTypeCallable(argTypes.map { it.instantiateTemplate(nameMap) }, returnType?.instantiateTemplate(nameMap))
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny      -> true
        is ExPhpTypeCallable -> true
        is ExPhpTypeNullable -> isAssignableFrom(rhs.inner, project)
        else                 -> false
    }
}
