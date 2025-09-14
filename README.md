# polybool-kotlin

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](httpw://kotlinlang.org)
![JVM](https://img.shields.io/badge/-JVM-gray.svg?style=flat)
![Android](https://img.shields.io/badge/-Android-gray.svg?style=flat)
![iOS](https://img.shields.io/badge/-iOS-gray.svg?style=flat)
![Windows](https://img.shields.io/badge/-Windows-gray.svg?style=flat)
![Linux](https://img.shields.io/badge/-Linux-gray.svg?style=flat)
![macOS](https://img.shields.io/badge/-macOS-gray.svg?style=flat)
![JS](https://img.shields.io/badge/-JS-gray.svg?style=flat)
![WASM](https://img.shields.io/badge/-WASM-gray.svg?style=flat)

This is a Kotlin Multiplatform port of
[polybool-java](https://github.com/Menecats/polybool-java) / [polybooljs](https://github.com/velipso/polybooljs).

For instructions on how to use polybool-kotlin, please see the original
project's README as well as the unit tests.

## What is PolyBool?

PolyBool is a library for performing boolean operations on 2D polygons - union,
intersection, difference, and XOR.

I use it personally to optimize vector paths of Oxygen Not Included maps.
By efficiently combining and simplifying complex shapes, it helps reduce
map complexity and save space, making the data easier to work with and
more efficient for further processing.

## Installation

It's available on Maven Central.

```
implementation("de.stefan-oltmann:polybool-kotlin:0.1.1")
```

## State

The code is functional, though not especially elegant.

It was largely produced using IntelliJ IDEA’s conversion tool, followed by significant manual cleanup.
There is still work to do. Handling of nullable types, in particular, remains incomplete.

Currently, the only unit test, `PolyBoolTest`, acts as a regression check against the original polybool-java library.
For validation, 10k maps from the game Oxygen Not Included are used, and they convert identically to how they do with the original polybool-java library.

## Contributions

Contributions to polybool-kotlin are welcome! If you encounter any issues,
have suggestions for improvements, or would like to contribute new features,
please feel free to submit a pull request.

## Acknowledgements

* JetBrains for making [Kotlin](https://kotlinlang.org).
* Davide Menegatti for making [polybool-java](https://github.com/Menecats/polybool-java).
* Sean Connelly for making [polybooljs](https://github.com/velipso/polybooljs).

## License

This project is licensed under the same license as polybool-java & polybooljs, which is the MIT license.

See the `LICENSE` file for more information.
