# Knomad

Knomad 是一个用于大模型 API 统一管理与解析的轻量级工具库。它旨在解决因各大模型提供商接口规范不一而导致的维护成本高、对接繁琐等问题，通过统一的 YAML 描述文件实现 API 的配置、校验与响应提取。

## 为什么选择 Knomad

在对接多个大模型服务（如 OpenAI、Anthropic、各种国产模型等）时，你是否常被这些问题困扰？

* **接口碎片化**：每个厂商的请求头、参数格式、响应结构完全不同。
* **开发重复度高**：每次接入新模型都要重新编写 HTTP 调用与 JSON 解析逻辑。
* **难以维护**：硬编码的 API 调用逻辑随着接入模型增多变得极其脆弱。

Knomad 通过“配置即代码”的理念，让你只需编写一份符合规范的 yaml 文件，即可实现对不同厂商接口的标准化调用与自动解析。

## 主要特性

* **统一规约**：通过简单的 YAML 模板定义 API 路由、请求参数及响应结构，彻底告别硬编码。
* **自动化校验**：集成 JSON Schema 校验，在请求发出前自动拦截不符合定义的配置错误。
* **跨平台/模型通用**：只要你能定义出对应的 YAML 格式，任何 RESTful 风格的 LLM 接口都能轻松接入。
* **智能提取**：内置 JSONPath 支持，轻松从复杂的 API 响应中提取所需的对话内容。
* **类型安全**：基于 Kotlinx Serialization 提供强类型模型支持。

## 架构概览

* **YamlParser**: 系统的入口，负责读取 YAML 文件并将其反序列化为 KnomadConfig 对象。
* **YamlValidator**: 核心守门人，利用 networknt/json-schema-validator 确保所有配置文件格式严谨、逻辑合法。
* **Model 层**: 定义了一套通用的 API 交互协议（变量、复合类型、端点），是连接配置文件与代码逻辑的桥梁。

## 快速开始

### 0. 引入依赖

```kotlin
implementation("top.song-mojing:knomad:2.0.0")
```

### 1. 编写配置文件 (test.yaml)

为特定的模型服务商创建一个 YAML 描述文件，定义好请求地址、格式及响应的提取规则：

```yaml
# yaml-language-server: $schema=schema.yaml

version: 'v1.0.0'

variables:
  ApiKey:
    type: String
    required: true
    description: API Key
  Model:
    type: String
    required: true
    description: 需要调用的模型
  Content:
    type: List
    required: true
    description: 对话列表

baseUrl: 'https://api.openai.com/v1'

endpoints:
  completions:
    path: '/chat/completions'
    method: POST
    request:
      headers:
        Authorization: 'Bearer ${{variables.ApiKey}}'
        'Content-Type': 'application/json'
        Accept: 'application/json'
      body:
        model: '${{variables.Model}}'
        messages: '{{variables.Content}}'
    response:
      - httpCode: 'type:success'
        type: 'application/json'
        values:
          - name: content
            type: Map
            path: '$.choices[0].message.content'
      - httpCode: 'code:403'
        type: 'application/json'
        values:
          - name: errMsg
            type: String
            path: '$.error.message'
```

### 2. 在代码中解析

参考：[测试](./src/test/kotlin/Main.kt)

## 贡献与开源

本项目欢迎任何形式的贡献。

本项目采用 Apache License 2.0 协议开源。