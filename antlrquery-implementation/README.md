# Antlr XQuery
## Summary

This project provides a set of tools to query text files parsed with any given antlr grammar using XQuery-based language.
Antlr itself provides limited implementation of XPath expressions, which makes it only natural to use XQuery as the guideline.

The afore mentioned tools include a command line tool `antlr-xquery-tool`, a language server following LSP `antlr-xquery-server`, and LSP client extention for vscode.

[XQuery specification 4.0](https://qt4cg.org/specifications/xquery-40/xquery-40.html) is followed for the most part,
with plans of using [updating expressions extension](https://www.w3.org/TR/xquery-update-30/)
for tree modyfing queries.
This is, however, an Antlr-centric adaptation of XQuery and as such it should change specification whenever it makes sense in context of ANTLR.

## Table of contents
- [Command line tool documentation](./docs/cli.md)
- [Query language documentation](./docs/antlr-xquery.md)
  - [Introduction](./docs/query-language/introduction.md)
  - [Examples](./docs/query-language/examples.md)
  - [Type system](./docs/query-language/type%20system.md)
  - [Expression implementation overview](docs/query-language/supported%20expressions.md)
  - [Function implementation overview](docs/query-language/supported%20functions.md)
- [Vscode extension](./docs/vscode-extention.md)

