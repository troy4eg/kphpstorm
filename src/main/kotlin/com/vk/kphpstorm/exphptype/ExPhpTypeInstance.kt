package com.vk.kphpstorm.exphptype

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpClassHierarchyUtils
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.resolve.types.PhpType

/**
 * 'A', '\asdf\name' — are instances (not primitives!)
 */
class ExPhpTypeInstance(val fqn: String) : ExPhpType {
    override fun toString() = fqn

    override fun toHumanReadable(expr: PhpPsiElement) =
            if (fqn.startsWith('\\')) PhpCodeInsightUtil.createQualifiedName(PhpCodeInsightUtil.findScopeForUseOperator(expr)!!, fqn)
            else fqn

    override fun toPhpType(): PhpType {
        return PhpType().add(fqn)
    }

    override fun getSubkeyByIndex(indexKey: String): ExPhpType? {
        return null
    }

    override fun instantiateTemplate(nameMap: Map<String, ExPhpType>): ExPhpType {
        return nameMap[fqn] ?: this
    }

    override fun isAssignableFrom(rhs: ExPhpType, project: Project): Boolean = when (rhs) {
        is ExPhpTypeAny       -> true
        is ExPhpTypePipe      -> rhs.isAssignableTo(this, project)
        is ExPhpTypeNullable  -> isAssignableFrom(rhs.inner, project)
        is ExPhpTypePrimitive -> rhs === ExPhpType.NULL || rhs === ExPhpType.OBJECT

        // rhs can be assigned if: rhs == lhs or rhs is child of lhs (no matter, lhs is interface or class)
        is ExPhpTypeInstance  -> rhs.fqn == fqn || run {
            val phpIndex = PhpIndex.getInstance(project)
            val lhsClass = phpIndex.getAnyByFQN(fqn).firstOrNull() ?: return false
            var rhsIsChild = false
            phpIndex.getAnyByFQN(rhs.fqn).forEach { rhsClass ->
                PhpClassHierarchyUtils.processSuperWithoutMixins(rhsClass, true, true) { clazz ->
                    if (PhpClassHierarchyUtils.classesEqual(lhsClass, clazz))
                        rhsIsChild = true
                    !rhsIsChild
                }
            }
            rhsIsChild
        }

        else                  -> false
    }
}
