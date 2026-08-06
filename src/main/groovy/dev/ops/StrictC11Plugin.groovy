package dev.ops

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.CppCompile
import org.gradle.api.tasks.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Input
import org.gradle.api.GradleException

class StrictC11Plugin implements Plugin<Project> {
    void apply(Project project) {
        def ext = project.extensions.create('strictC11', StrictC11Extension)
        
        project.tasks.register('generateStrictC11Report', GenerateStrictC11ReportTask)
        project.tasks.register('runClangTidyFix', RunClangTidyFixTask)
        project.tasks.register('notifySlack', NotifySlackTask)

        project.afterEvaluate {
            project.tasks.withType(CppCompile).configureEach { task ->
                if (!project.plugins.hasPlugin('cpp')) {
                    throw new GradleException("The 'cpp' plugin must be applied to use strict-c11-plugin.")
                }
                def std = ext.standard
                boolean isMSVC = task.compiler.path.contains("cl.exe")
                
                if (isMSVC) {
                    task.compilerArgs.addAll(["/std:$std", "/permissive-", "/W4"])
                    if (ext.warningsAsErrors) task.compilerArgs.add("/WX")
                    task.compilerArgs.addAll(["/wd4200", "/wd4204"])
                    if (ext.enableMSVCFixes) {
                        task.compilerArgs.addAll(["/we4013", "/we4127", "/we4706"])
                    }
                } else {
                    def flags = ["-std=$std"]
                    if (ext.pedantic) flags.add("-pedantic-errors")
                    if (ext.warningsAsErrors) flags.add("-Werror")
                    flags.addAll(["-Wall", "-Wextra", "-Wconversion", "-Wsign-conversion"])
                    task.compilerArgs.addAll(flags)
                }
            }
        }
    }
}

class GenerateStrictC11ReportTask extends DefaultTask {
    @OutputFile
    File reportFile = project.file("build/reports/strict-c11.html")

    @TaskAction
    void generate() {
        // Mock violations – in real use, parse the compiler logs
        def violations = [
            [
                file: "src/main.c",
                line: 5,
                column: 3,
                severity: "error",
                code: "Wpedantic",
                message: "GNU extension: __attribute__ used",
                snippet: "int main() {\n    __attribute__((unused)) int x = 0;\n    return 0;\n}",
                snippetLineIndex: 1
            ]
        ]
        reportFile.parentFile.mkdirs()

        def escapeHtml = { String s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;") }
        def getStandardRef = { String code ->
            def map = ["C4013": "§6.5.2.2", "C4127": "§6.8.4.1", "C4706": "§6.5.16", "Wpedantic": "§4"]
            map.getOrDefault(code, "")
        }

        reportFile.withWriter { writer ->
            def xml = new groovy.xml.MarkupBuilder(writer)
            xml.html {
                head {
                    meta(charset: "UTF-8")
                    title("Strict C11 Compliance Report")
                    style {
                        """
                        body { font-family: system-ui, sans-serif; margin: 2rem; background: #f8f9fa; }
                        h1 { color: #1a1a1a; }
                        .summary { display: flex; gap: 2rem; background: #fff; padding: 1rem; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 2rem; }
                        .stat { font-size: 1.2rem; }
                        .stat span { font-weight: bold; }
                        .error { color: #d32f2f; }
                        .warning { color: #f57c00; }
                        table { width: 100%; border-collapse: collapse; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
                        th, td { padding: 0.75rem; text-align: left; border-bottom: 1px solid #e0e0e0; }
                        th { background: #e9ecef; font-weight: 600; }
                        tr:hover { background: #f1f3f5; }
                        .snippet { background: #272822; color: #f8f8f2; padding: 0.5rem; border-radius: 4px; font-family: monospace; font-size: 0.9rem; overflow-x: auto; }
                        .snippet .highlight { background: #ff6b6b; color: #000; font-weight: bold; }
                        .ref { font-size: 0.8rem; color: #6c757d; font-family: monospace; }
                        """ .stripIndent()
                    }
                }
                body {
                    h1("📋 Strict C11 Compliance Report")
                    def errors = violations.count { it.severity == "error" }
                    def warnings = violations.count { it.severity == "warning" }
                    div(class: "summary") {
                        div(class: "stat") { span("Errors: "); span(errors, class: "error") }
                        div(class: "stat") { span("Warnings: "); span(warnings, class: "warning") }
                        div(class: "stat") { span("Compliance Score: "); span("${Math.round((1 - (errors + warnings) / (violations.size() + 1)) * 100)}%", style: "color: #1976d2;") }
                    }
                    if (violations.isEmpty()) {
                        p("✅ All files conform to strict C11!", style: "color: #2e7d32; font-weight: bold;")
                    } else {
                        table {
                            thead { tr { th("File"); th("Line"); th("Col"); th("Code"); th("Standard Ref"); th("Message"); th("Source") } }
                            tbody {
                                violations.each { v ->
                                    tr {
                                        td(v.file); td(v.line); td(v.column)
                                        td(v.code, class: v.severity == "error" }
                    def warnings = violations.count { it.severity == "warning" }
                    div(class: "summary") {
                        div(class: "stat") { span("Errors: "); span(errors, class: "error") }
                        div(class: "stat") { span("Warnings: "); span(warnings, class: "warning") }
                        div(class: "stat") { span("Compliance Score: "); span("${Math.round((1 - (errors + warnings) / (violations.size() + 1)) * 100)}%", style: "color: #1976d2;") }
                    }
                    if (violations.isEmpty()) {
                        p("✅ All files conform to strict C11!", style: "color: #2e7d32; font-weight: bold;")
                    } else {
                        table {
                            thead { tr { th("File"); th("Line"); th("Col"); th("Code"); th("Standard Ref"); th("Message"); th("Source") } }
                            tbody {
                                violations.each { v ->
                                    tr {
                                        td(v.file); td(v.line); td(v.column)
                                        td(v.code, class: v.severity == "error" ? "error" : "warning")
                                        td(getStandardRef(v.code), class: "ref")
                                        td(v.message)
                                        td {
                                            def lines = v.snippet.split("\n")
                                            pre(class: "snippet") {
                                                lines.eachWithIndex { line, i ->
                                                    if (i == v.snippetLineIndex) span(line, class: "highlight") else span(line)
                                                    span("\n")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        println("📊 Report generated at: ${reportFile.absolutePath}")
    }
}

class RunClangTidyFixTask extends DefaultTask {
    @TaskAction
    void runFix() {
        def buildDir = project.buildDir.absolutePath
        def compileCommands = project.file("$buildDir/compile_commands.json")
        if (!compileCommands.exists()) {
            throw new GradleException("compile_commands.json not found. Ensure the 'cpp' plugin is applied.")
        }
        def sourceFiles = project.fileTree("src").matching { include("**/*.c", "**/*.h") }.files
        def checks = "-*,google-*,cppcoreguidelines-*,bugprone-*,cert-*,hicpp-*,modernize-*,readability-*,portability-*"
        def cmd = ["clang-tidy", "-p", buildDir, "-checks=$checks", "--fix", "--fix-errors"]
        cmd.addAll(sourceFiles.collect { it.absolutePath })
        if (project.hasProperty("dryRun")) cmd.add("--fix-notes")
        println("🚀 Running: ${cmd.joinToString(" ")}")
        def process = new ProcessBuilder(cmd).directory(project.projectDir).redirectErrorStream(true).start()
        def output = process.inputStream.bufferedReader().readText()
        println(output)
        if (process.waitFor() != 0) throw new GradleException("clang-tidy exited with errors")
        println("✅ clang-tidy fixes applied.")
    }
}

import java.net.HttpURLConnection
import java.net.URL

class NotifySlackTask extends DefaultTask {
    @TaskAction
    void notify() {
        def webhook = System.getenv("SLACK_WEBHOOK_URL") ?: project.findProperty("slackWebhook") ?: ""
        if (webhook.isEmpty()) {
            println("⚠️ SLACK_WEBHOOK_URL not set. Skipping Slack notification.")
            return
        }
        def jsonFile = project.file("build/reports/violations.json")
        if (!jsonFile.exists()) {
            sendSlack(webhook, "✅ Strict C11 Check passed with 0 violations! 🎉")
            return
        }
        def violations = new groovy.json.JsonSlurper().parse(jsonFile)
        def errors = violations.count { it.severity == "error" }
        def warnings = violations.count { it.severity == "warning" }
        def message = """
            |🔴 **Strict C11 Compliance Report**  
            |Errors: *$errors*  
            |Warnings: *$warnings*  
            |<${project.buildDir.absolutePath}/reports/strict-c11.html|📄 View Full Report>  
        """.stripMargin()
        sendSlack(webhook, message)
    }

    private void sendSlack(String url, String text) {
        def payload = new groovy.json.JsonBuilder(
            text: text, mrkdwn: true,
            attachments: [[color: text.contains("Passed") ? "good" : "danger", fields: [[title: "Project", value: project.name, short: true]]]]
        ).toPrettyString()
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.outputStream.withWriter { writer -> writer.write(payload) }
        if (conn.responseCode != 200) throw new GradleException("Slack webhook returned ${conn.responseCode}")
        println("Slack notification sent.")
    }
}
