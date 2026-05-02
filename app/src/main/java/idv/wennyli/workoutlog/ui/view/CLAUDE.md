# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ViewModel 字串資源

ViewModel 不直接依賴 `Context`，改透過 `AppResource` 介面（由 `ResourceProvider` 實作）取得字串資源，測試時可用 MockK mock 替換。
