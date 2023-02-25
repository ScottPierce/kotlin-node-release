# kotlin-node-slim

## What does this Gradle Plugin do?
When releasing a module in Kotlin JS, all dependencies from all other js projects are included in the `node_modules`
directory. This adds significant bloat when trying to package a Kotlin nodejs application in a docker container, or
ship it as a standalone application.

This plugin iterates over the dependencies of the module recursively, collects all referenced npm dependencies,
and then generates a small npm project.

* 0.0.4 - Kotlin 1.7.x
* 0.0.5 - Kotlin 1.8.x

## Gradle Project Setup
Here is an example of how to use this with Kotlin Script Gradle:
```Kotlin
plugins {
    kotlin("multiplatform")
    id("kotlin-node-slim")
}
```

```Kotlin
kotlinNodeSlim {
    scripts.put("prod", "yarn supervisor {{main}}") // Add a script to the package.json
    outputDir.set(File(buildDir, "slimDistributable")) // Default Value
}
```

## Generating a slim distributable
To generate a slim js project, run the gradle task `compileNodeJsSlimDistribution`. This will typically look something
like this:
```Shell
./gradlew :<your kotlin node gradle module name>:compileNodeJsSlimDistribution
```

The project will be generated in the given `outputDir`, which defaults to `./build/slimDistributable`.

After generating the slim distributable, you just run `npm install` and it should be good to go.
