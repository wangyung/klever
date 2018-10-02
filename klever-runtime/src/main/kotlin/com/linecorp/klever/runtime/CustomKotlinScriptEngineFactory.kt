package com.linecorp.klever.runtime

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import org.jetbrains.kotlin.script.util.scriptCompilationClasspathFromContextOrStlib
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine

class CustomKotlinScriptEngineFactory : KotlinJsr223JvmScriptEngineFactoryBase() {
    private val logger: Logger = LoggerFactory.getLogger("ScriptEngine")
    override fun getScriptEngine(): ScriptEngine {
        val classPath = scriptCompilationClasspathFromContextOrStlib("kotlin-script-util.jar", wholeClasspath = true)
        logger.info("all class path: $classPath")

        return KotlinJsr223JvmLocalScriptEngine(
                this,
                classPath,
                KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
                { ctx, types ->
                    ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray())
                },
                arrayOf(Bindings::class)
        )
    }

}
