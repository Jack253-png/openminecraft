package com.primogemstudio.engine.jmake

import com.primogemstudio.engine.i18n.Internationalization.tr
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class Toolchain {
    GCC, CLANG
}

class BaseProjectBuilder(
    private val projBase: File,
    val buildDir: File,
    private val toolchain: Toolchain,
    files: List<String>,
    private val includes: List<String>,
    private val target: File,
    private val linkLibs: List<File>,
    val resultCallback: (String, Double) -> Unit
) : ProjectBuilder {
    private val defines: MutableMap<String, Any> = mutableMapOf()
    private val configs: MutableMap<String, Any> = mutableMapOf()
    private val patts = files.map { Pattern.compile(it) }
    override fun checkEnv() {
        if (toolchain == Toolchain.GCC && (!File("/usr/bin/g++").exists() || !File("/usr/bin/gcc").exists())) {
            throw IllegalStateException(tr("exception.jmake.env_jmake_gcc.corrupt"))
        } else if (!File("/usr/bin/clang++").exists() || !File("/usr/bin/clang").exists()) {
            throw IllegalStateException(tr("exception.jmake.env_jmake_clang.corrupt"))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun buildProject(): List<CommandPropI> {
        val fileMap = mutableMapOf<File, File>()
        Files.walkFileTree(projBase.toPath(), object : FileVisitor<Path> {
            override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult =
                FileVisitResult.CONTINUE

            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                var inc = false
                patts.forEach {
                    if (!inc && it.matcher(file.toString()).find()) inc = true
                }
                if (inc && file != null) {
                    fileMap[file.toFile()] =
                        buildDir.resolve("${Base64.encode(file.toString().toByteArray(Charsets.UTF_8))}.o")
                }

                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult = FileVisitResult.CONTINUE
            override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult = FileVisitResult.CONTINUE
        })

        val chain = if (toolchain == Toolchain.GCC) "/usr/bin/gcc" else "/usr/bin/clang"
        val chainxx = if (toolchain == Toolchain.GCC) "/usr/bin/g++" else "/usr/bin/clang++"

        return mutableListOf<CommandPropI>(
            MultiCommandProp(
                fileMap.map { (t, u) ->
                    CommandProp(projBase, mutableListOf(
                        chain, "-c", "-fPIC", t.toString(), "-o", u.toString()
                    ).apply {
                        includes.forEach { add("-I$it") }
                        defines.forEach { (t, u) -> add("-D$t=$u") }
                    })
                },
                configs["JOBS"]?.toString()?.toInt() ?: 1,
                fileMap.map { (t, _) -> t.path }
            )
        ).apply {
            add(
                CommandProp(
                    projBase, mutableListOf(if (fileMap.keys.map { it.toString() }
                            .any { it.endsWith(".cpp") || it.endsWith(".cppm") }) chainxx else chain).apply {
                        fileMap.values.forEach { t -> add(t.path) }
                        linkLibs.forEach { t -> add(t.path) }
                        add("-shared")
                        add("-o")
                        add(target.path)
                    }
                )
            )
        }
    }

    override fun addDefine(key: String, value: Any) {
        defines[key] = value
    }

    override fun config(key: String, value: Any) {
        configs[key] = value
    }

    override fun outputProcessor(data: String) {
        val d = data.split("&")
        resultCallback(
            if (d.size == 3) d[2] else tr("jmake.objs.dll.name", target.path),
            d[0].toDouble() / d[1].toDouble()
        )
    }
}