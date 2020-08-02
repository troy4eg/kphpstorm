package com.vk.kphpstorm.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ArrayIndex
import com.jetbrains.php.lang.psi.elements.PhpReference
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

/**
 * Main class for completion, that is listed in <completion.contributor> in plugin.xml
 * (order="first" is significant, this makes runRemainingContributors() work)
 */
class KphpStormCompletionContributor : CompletionContributor() {
    init {
        // @kphp-... doc tags
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TAG_NAME),
                KphpDocTagNameCompletionProvider())
        // type inside @param/@var/@return and tested types
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withParent(PhpReference::class.java),
                ExPhpTypeCompletionProvider())
        // #ifndef KittenPHP
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PhpTokenTypes.LINE_COMMENT),
                IfndefEndifCompletionProvider())
        // shape(['...']) — key names depending on context when creating the shape
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withParent(StringLiteralExpression::class.java).withSuperParent(3, ArrayCreationExpression::class.java),
                ShapeKeyInvocationCompletionProvider())
        // $sh['...'] - key names when accessing the shape
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withParent(StringLiteralExpression::class.java).withSuperParent(2, ArrayIndex::class.java),
                ShapeKeyUsageCompletionProvider())
    }
}
