# 接口与公共规范约定文档（API Contract A）

- **版本**：v0.3（第一阶段公共基线冻结版）
- **适用范围**：campus-evaluation-system 前后端全部 HTTP 接口（context-path 为 `/api`，下文路径均为相对 `/api` 的路径）
- **状态**：本文已冻结**第一阶段“接口与公共规范冻结”公共基线**，作为当前前后端开发与联调的唯一协议依据。本文不宣称 A/B 全部业务完成；平台业务、全量权限矩阵、跨租户全量联调和产品级 v1.0 冻结仍属于后续验收范围。
- **规范定位（重要）**：正文各节描述的是**目标规范（应然）**；各节下的"🟢/🟡/🔴 现状"标注描述的是**当前后端代码的实现进度（实然）**。二者是不同维度——代码尚未对齐**不影响**规范权威性，仅表示存在待落地工程项。
- **版本门槛**：
  - `v0.x 评审稿`：规范内容成形，但仍有未落地的代码差异或待议项。
  - `v0.3 公共基线冻结版`：本阶段公共响应、错误、分页、用户字段、前端适配和兼容策略已固定；未纳入本阶段的业务能力继续单独验收。
  - `v1.0 产品冻结版`：A/B 业务范围、四类角色、两个租户和全量接口联调通过；此后破坏性变更需升版本号并评审。
- **标注图例**：🟢 已实现并符合规范 / 🟡 部分实现（存在差异） / 🔴 未实现或不一致（待落地/待整改）。

## 变更记录

| 版本 | 说明 |
| --- | --- |
| v0.1 | 首版评审稿，覆盖响应/分页/用户/角色/状态/错误码/租户公共规则。 |
| v0.2 | 采纳评审意见：①确认 HTTP 状态码与业务 code 对齐策略；②明确 `timestamp` 保留与格式化方案；③分页/用户字段给出正式迁移方案；④补齐附件协议、接口权限矩阵、接口总清单与示例；⑤收敛错误码（弃用 600 段，改用 HTTP 语义码 + 稳定错误标识）；⑥明确租户默认回退安全整改项。 |
| v0.3 | 冻结第一阶段公共协议基线：统一响应/错误/分页/角色字段/状态值/兼容策略，补充后端协议测试；未完成的业务与产品级联调项保留在后续验收清单。 |

---

## 1. 统一响应格式与 HTTP 状态码策略

### 1.1 响应体结构

所有 JSON 接口统一返回如下结构（含成功与失败）：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-09-25 12:00:00"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | int | 是 | 业务状态码，与 HTTP 状态码语义对齐（见 §1.2），`200` 表示成功。 |
| message | String | 是 | 提示信息。成功固定 `success`；失败为可读中文说明。 |
| data | Object/Array/null | 是 | 业务数据，无数据时为 `null`，字段不得省略。 |
| timestamp | String | 是 | 服务器时间，固定 `yyyy-MM-dd HH:mm:ss`（时区 `Asia/Shanghai`）。 |
| errKey | String | 否 | 稳定机器可读错误标识（如 `DUPLICATE_SUBMISSION`），仅失败且需要程序化判断时返回，见 §6.2。 |

### 1.2 HTTP 状态码策略（冻结决策：对齐，不伪装）

**结论：HTTP 状态码与业务 `code` 一一对齐，不再把所有业务错误伪装成 HTTP 200。** 前端只保留一套拦截规则。

| 场景 | HTTP 状态 | 业务 code |
| --- | --- | --- |
| 成功 | 2xx（通常 200） | 200 |
| 未登录 / token 失效 | 401 | 401 |
| 无权限 / 无角色 / 租户越权 | 403 | 403 |
| 参数错误 / 校验失败 | 400 | 400 |
| 资源不存在（本租户内） | 404 | 404 |
| 资源/状态冲突（重复提交、账号重复等） | 409 | 409 |
| 业务校验失败（语义错误，可展示） | 422 | 422 |
| 请求方法不支持 | 405 | 405 |
| 系统内部错误 | 500 | 500 |

**前端处理规则（唯一）**：先看 `response.status`（网络/认证层），非 2xx 直接按对应语义处理（401→跳登录；403→无权限提示）；2xx 时再解析响应体 `code`。`code !== 200` 视为业务失败并展示 `message`。**禁止**同时依赖 `status===401` 与 `data.code===401` 两套逻辑。

> **例外（非 JSON 响应）**：文件下载 `GET /files/{id}/download`、预览 `GET /files/{id}/preview` 成功时返回二进制流（HTTP 200 + `Content-Type`），失败时返回对应 HTTP 状态码（见 §8）。健康检查、Swagger 等非业务 JSON 接口不受 `code/message/data` 结构约束。

> **落地对齐**
> - 🟢 `R<T>` 已新增 `timestamp`，`ok/fail` 均填充服务器时间（`yyyy-MM-dd HH:mm:ss`）。
> - 🟢 已注册全局 `JacksonConfig`，`LocalDateTime` 统一按 `yyyy-MM-dd HH:mm:ss` 序列化。
> - 🟢 `SaTokenExceptionHandler` 的 401/403 与 `GlobalExceptionHandler` 的 `BusinessException` 已返回真实 HTTP 状态，body `code` 与 HTTP status 对齐。
> - 🟢 参数校验（`MethodArgumentNotValid/Bind`→400）、`405`、`404`、兜底 `500` 已是真实 HTTP 状态，符合对齐方向。

---

## 2. 统一分页格式与迁移方案

### 2.1 响应结构

所有分页列表接口的 `data` 统一为：

```json
{ "records": [], "total": 0, "page": 1, "pageSize": 20 }
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| records | Array | 是 | 当前页数据，空结果返回 `[]`（不为 `null`）。 |
| total | long | 是 | 总记录数。 |
| page | int | 是 | 当前页码，从 1 开始。 |
| pageSize | int | 是 | 每页大小，默认 20，最大 100。 |

`totalPages` 作为**附加只读字段**保留（前端不依赖，可用于自检）。

### 2.2 请求入参

统一使用查询参数 `page`（默认 1）、`pageSize`（默认 20）。关键字统一 `keyword`，状态统一 `status`。

### 2.3 迁移方案（冻结决策：正式改后端 DTO，不靠 `@JsonProperty` 临时兜底）

1. **新增统一入参基类 `PageQuery`**（字段 `page`/`pageSize`），分页 Controller 对外统一接受 `page`，迁移期保留 `pageNum` 兼容。
2. **`PageResult` 字段 `pageNum → page`**（正式改名），保留 `totalPages`。
3. **过渡期双写**：迁移窗口内 `PageResult` 同时输出 `page` 与 `pageNum`（值相同），`PageQuery` 同时接收 `page` 与 `pageNum`（`page` 优先）；前端 API 层全量切换到 `page` 后，删除 `pageNum` 别名。
4. **兼容清单与截止**：受影响的当前分页接口见 §10（全部使用 `pageNum`）。**要求前端在同一次联调中一并切换**，不长期保留双字段；旧字段最迟在 v1.0 冻结发布时移除。

> **落地对齐**
> - 🟢 `PageResult` 已双写 `page` 与 `pageNum`，`totalPages` 保留。
> - 🟢 分页接口已接受正式入参 `page/pageSize`，迁移期兼容 `pageNum`。
> - 🟢 已新增统一 `PageQuery` 基类，`page` 优先、`pageNum` 兼容。

---

## 3. 统一用户字段与迁移方案

`/auth/login`、`/auth/me`、各用户列表接口统一使用下列驼峰字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| userId | Long | 用户主键。 |
| username | String | 登录账号。 |
| realName | String | 真实姓名。 |
| roleType | String | 主类型：`system_admin`/`school_admin`/`staff`/`student`。 |
| roles | String[] | **角色编码字符串数组**（非对象数组），见 §4，无值返回 `[]`。 |
| permissions | String[] | 权限编码数组，无值返回 `[]`。 |
| tenantId | Long | 租户 ID，由后端确定（§7）。 |
| schoolId | Long | 学校 ID，由后端确定。 |
| mustChangePassword | Boolean | 是否强制改密。 |
| avatarUrl | String | 头像 URL，无则 `null`。 |

### 3.1 迁移方案（冻结决策）

1. **后端正式字段为 `roleType`**；`LoginResponse` 与 `CurrentUserVO` 由 `userType` **改名为 `roleType`**。
2. **过渡期双写**：短期内 `roleType` 与 `userType` 同时返回（值相同）。前端统一从**单一适配层**读取（该层优先取 `roleType`，回退 `userType`），禁止在业务代码/路由里散落 `role_type`/`role`/`userType` 多路取值。
3. **`roles` 明确为角色编码字符串数组**（`List<String>`），不使用角色对象数组。
4. **兼容字段删除时间**：前端适配层全量切换并回归通过后，`userType` 最迟在 v1.0 冻结发布时移除。
5. **登录、`/auth/me`、用户列表使用同一套驼峰字段**，命名与大小写完全一致。

> **落地对齐**
> - 🟢 `LoginResponse`、`CurrentUserVO` 已提供 `roleType`，迁移期继续双写 `userType`。
> - 🟢 `/school/roles/options` 已接受 `roleType`，迁移期兼容 `userType`。
> - 🟢 `roles`/`permissions` 已是 `List<String>`，符合编码数组约定。

---

## 4. 统一角色编码

四类**基础角色**：

| 角色编码 | 名称 | 说明 |
| --- | --- | --- |
| system_admin | 系统管理员 | 平台级，可跨租户访问平台数据，不可绕过审计（§7）。 |
| school_admin | 学校管理员 | 单个租户/学校的最高业务管理员。 |
| staff | 教职工 | 学校内教职工基础角色。 |
| student | 学生 | 学生基础角色。 |

**教职工子角色**（隶属 `staff` 体系，用独立角色编码/权限编码区分职责）：

| 子角色编码 | 名称 |
| --- | --- |
| teaching_admin | 学院教学管理员 |
| service_admin | 后勤部门管理员 |
| feedback_handler | 反馈处理员 |
| form_publisher | 评价表单发布员 |

**约定细则**
- 鉴权（Sa-Token `checkRole`）使用**英文角色编码**。
- 前端菜单/接口级用 `roles`，按钮级用 `permissions`。
- 新增子角色不改变四类基础角色语义。

> **落地对齐**
> - 🟢 `auth_role` 表已含全部 8 个编码。
> - 🔴 当前仅 `SaTokenConfigure` 以**路由前缀**粗粒度限制 `school_admin`/`student`；`/school/*` 业务 CRUD、`/evaluation/forms`、`/files` 等**未做接口级角色/权限校验**（见 §9 权限矩阵整改）。

---

## 5. 统一状态值

状态字段统一使用**小写英文字符串**存储与传输，中文仅用于展示（`xxxLabel`）。

### 5.1 用户状态（AuthUserAccount.status）
`active` 正常 / `disabled` 停用 / `locked` 锁定。
> 🟢 现状：`ChangeUserStatusDTO` 已统一入参为 `active`/`disabled`/`locked`，不再接受 `enabled` 别名。

### 5.2 租户状态（pf_tenant.status）
`pending` 待激活 / `active` 正常 / `suspended` 暂停 / `expired` 已过期。
> 🟢 现状：默认 `active`，登录校验 `status='active'`。

### 5.3 入驻申请状态（pf_school_onboarding_application.status）
`pending` / `approved` / `rejected` / `resubmit_required`。
> ⚪ 规划中：`/platform/onboarding/*` 接口尚未实现（§10）。

### 5.4 审核/表单状态
`draft` 草稿 / `pending` 待审核 / `approved` 通过 / `rejected` 驳回 / `published` 已发布。
> 🟢 评价表单与审核流已使用上述值。

### 5.5 逻辑删除
统一 `deleted`：`0` 未删除 / `1` 已删除（🟢 全线 `@TableLogic`）。
> ⚠️ 唯一索引注意：软删除记录仍占用唯一键；"防重复提交"等唯一约束业务须评估物理删除或将 `deleted` 纳入唯一键。

### 5.6 鉴权错误语义
未登录→401；无权限/无角色→403；租户越权→403（不泄露存在性）。HTTP 状态与 code 对齐（§1.2）。

### 5.7 租户越权信息保护
跨租户统一 `403`，**不得**对"他租户资源"返回 `404`（避免探测）。按 ID 操作若 `tenantId` 不符一律 `403`，message 统一"没有操作权限"。
> 🔴 现状：部分服务对"不存在"与"跨租户"分别返回 `404`/`403`。需统一为"先按 `id + tenantId` 查询，查不到即 `403`"。

---

## 6. 统一错误码与错误标识（收敛决策）

### 6.1 冻结的业务 code（采用 HTTP 语义码，**弃用 600 段**）

> 决策理由：§1.2 已确定 HTTP 状态与 code 对齐，`600` 无对应 HTTP 语义会造成映射空洞，故不再引入 600 段，改用 HTTP 语义码 + 稳定错误标识。

| code | HTTP | 含义 | 典型场景 |
| --- | --- | --- | --- |
| 200 | 200 | 成功 | 正常返回 |
| 400 | 400 | 参数错误 | 校验失败、格式错误 |
| 401 | 401 | 未认证 | 未登录 / token 失效 |
| 403 | 403 | 无权限/跨租户 | 无角色、无权限、租户越权 |
| 404 | 404 | 资源不存在 | 本租户内确不存在 |
| 405 | 405 | 方法不支持 | HTTP Method 不匹配 |
| 409 | 409 | 资源/状态冲突 | 重复提交、账号重复、状态不允许 |
| 422 | 422 | 业务校验失败 | 导入失败、语义校验不通过 |
| 500 | 500 | 系统内部错误 | 兜底异常、未预期错误 |
| 503 | 503 | 依赖不可用 | 健康检查 DB/Redis 异常 |

> 🟢 现状：`ErrorCode` 已补 `CONFLICT(409)`、`UNPROCESSABLE(422)`、`SERVICE_UNAVAILABLE(503)`，并停用 `600`；`BusinessException` 支持显式 code 与 `errKey`。

### 6.2 稳定错误标识 `errKey`（可选字段）

对需要前端程序化判断的错误，失败响应附带 `errKey`（大写下划线，稳定不随文案变化）。首批冻结：

| errKey | code | 含义 |
| --- | --- | --- |
| `UNAUTHORIZED` | 401 | 未登录/过期 |
| `FORBIDDEN` | 403 | 通用无权限 |
| `TENANT_FORBIDDEN` | 403 | 跨租户越权 |
| `USER_DISABLED` | 422 | 账号被停用/锁定 |
| `DUPLICATE_SUBMISSION` | 409 | 重复提交评价 |
| `STATE_CONFLICT` | 409 | 状态不允许当前操作 |
| `USERNAME_EXISTS` | 409 | 账号重复 |
| `IMPORT_FAILED` | 422 | 批量导入失败 |
| `EVAL_WINDOW_NOT_STARTED` | 422 | 评价窗口未开始 |
| `EVAL_WINDOW_ENDED` | 422 | 评价窗口已结束 |

> 说明：`errKey` 为渐进增强项，非强制；未提供时前端仅按 `code` + `message` 处理。

---

## 7. 统一租户规则与安全整改

1. **上下文来源唯一**：`tenantId`/`schoolId` 由后端登录时确定（`AuthServiceImpl.login` 依据账号 `tenantId` 反查 `schoolId`），写入 Sa-Token Session 的 `LoginUser`。
2. **Header 不覆盖登录态**：`X-Tenant-Id`/`X-School-Id` **仅未登录场景**作回退；已登录一律以 `LoginUser` 为准。
3. **禁止跨租户访问**：普通学校用户禁止访问他租户数据；所有学校业务查询必须带 `tenant_id`（涉校再加 `school_id`）过滤。
4. **系统管理员边界**：`system_admin` 可访问平台级数据，但操作同样写审计日志，不得绕过。
5. **按 ID 操作必须校验归属**：`getById/update/delete` 服务层校验资源 `tenantId` 与上下文一致，不一致按 §5.7 返回 403。
6. **操作日志**：写操作经 `@OperationLog` 记录操作人 `userId`、`tenantId`、动作、目标、时间。

### 7.1 受保护接口不得回退默认租户（安全整改，冻结决策）

- **公开接口**（§10 标 `公开`）：允许无租户上下文，或使用 Header 指定租户（如登录前租户选择）。
- **受保护接口**：无登录用户时必须 **401**，**禁止**静默使用默认租户；已登录用户 Header **不得**覆盖 Token 中的租户。
- 因此 `CommonConstants.DEFAULT_TENANT_ID` **不得**作为受保护接口的兜底租户。

> **落地对齐**
> - 🟡 `TenantInterceptor` 已实现"登录态优先、Header 次之"，符合 §7.2。
> - 🟢 已移除无登录且无 Header 时的 `DEFAULT_TENANT_ID=1` 静默回退；受保护接口上下文缺失按未登录（401）处理。
> - 🟡 各业务 Service 手写 `.eq(tenantId,...)`，建议评估启用 MyBatis-Plus `TenantLineInnerInterceptor` 自动注入（特殊表白名单）。
> - 🔴 逐一排查按 ID 的查询/修改/删除是否均做 `id + tenantId` 归属校验（§5.7）。

---

## 8. 附件 / 文件协议（补齐冻结）

### 8.1 上传接口

| 项 | 约定 |
| --- | --- |
| 路径 | `POST /files/upload` |
| Content-Type | `multipart/form-data` |
| 文件参数名 | `file`（必填，单个） |
| 业务类型参数 | `bizType`（选填，string，见 §8.3） |
| 认证 | 需登录（受保护接口，租户由上下文决定） |
| 响应 | `R<FileResourceVO>`（§8.4） |
| 批量 | 当前为单文件；多文件由前端多次调用，暂不提供批量端点 |

### 8.2 大小与类型限制

| 项 | 值 | 来源 |
| --- | --- | --- |
| 单文件大小上限 | **50 MB** | `spring.servlet.multipart.max-file-size` |
| 单次请求总大小上限 | **100 MB** | `spring.servlet.multipart.max-request-size` |
| 扩展名白名单 | `jpg, jpeg, png, webp, gif, pdf, doc, docx, xls, xlsx` | `FileResourceServiceImpl.ALLOWED_EXTENSIONS` |

- 类型校验以**文件扩展名**为准（当前实现）；超限由 Spring 抛 `MaxUploadSizeExceededException`。
- 🟡 后续整改项：仅凭扩展名不足以防伪造，建议增加 MIME/魔数头校验。
- 🟢 `MaxUploadSizeExceededException` 已纳入全局处理，映射为 `422` + `errKey=FILE_TOO_LARGE`。

### 8.3 bizType（业务类型）

`bizType` 为受控字符串枚举，用于分目录存储（`files/{bizType}`）与按业务检索。首批冻结值（可扩展）：

| bizType | 用途 |
| --- | --- |
| `avatar` | 用户头像 |
| `school_logo` | 学校 Logo |
| `eval_form` | 评价表单附件 |
| `feedback` | 反馈凭证 |
| `general` | 未指定时的默认目录 |

> 前端上传时**必须**传语义化 `bizType`；缺省落 `general`。

### 8.4 返回字段（FileResourceVO）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 文件 ID（`fileId`，前端引用主键）。 |
| fileName | String | 原始文件名。 |
| mimeType | String | MIME 类型。 |
| url | String | 访问 URL（预览/下载可用地址）。 |
| size | Long | 字节数。 |
| bizType | String | 业务类型。 |
| bizId | Long | 关联业务 ID（可空，绑定后回填）。 |
| uploaderId | Long | 上传人。 |
| tenantId / schoolId | Long | 归属租户/学校。 |
| createdAt / updatedAt | String | `yyyy-MM-dd HH:mm:ss`。 |

- **前端只引用 `id` 与 `url`**。`objectKey`（对象存储内部键）**不下发给前端**（仅实体内部字段）。
- 前端拿到 `id` 后，业务提交时携带 `fileId`；后端在业务落库时回填 `bizId` 完成绑定。

### 8.5 下载、预览与访问权限

| 操作 | 路径 | 认证 | 租户校验 |
| --- | --- | --- | --- |
| 元数据 | `GET /files/{id}` | 需登录 | ✅ `checkAccess`：跨租户 403 |
| 下载 | `GET /files/{id}/download` | 需登录 | ✅ 跨租户 403 |
| 预览 | `GET /files/{id}/preview` | 需登录 | 🟢 已按文件归属校验 |
| 删除 | `DELETE /files/{id}` | 需登录 | ✅ 跨租户 403 |

> 🟢 现状：`preview` 已移出公开放行名单，并与下载同级执行登录与归属校验。

### 8.6 存储、删除与临时/永久文件

- 存储后端：`campus.storage.type=local`，本地目录 `./uploads`，URL 前缀 `/upload`（可切换对象存储，契约只依赖返回的 `url`/`id`）。
- 删除策略：`DELETE /files/{id}` **仅逻辑删除数据库记录，不删物理文件**（当前实现）。
- 临时/永久：上传即为"未绑定"（`bizId=null`）临时态；业务提交成功后回填 `bizId` 转永久态。
  > 🔴 待落地：需定时清理任务回收"长期未绑定 `bizId`"的临时文件（含逻辑删除后的物理文件延迟清理），须在 v1.0 前给出方案。

---

## 9. 接口权限矩阵（逐接口冻结）

图例：🔓公开（无需登录）｜🔑需登录。"目标角色/权限"为应然要求，"现状"为实然差异。

| 接口 | 方法 | 登录 | 目标角色/权限 | 数据范围 | system_admin | 审计 | 现状 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `/auth/login` | POST | 🔓 | - | - | 允许 | - | 🟢 |
| `/auth/captcha` | GET | 🔓 | - | - | 允许 | - | 🟢 |
| `/auth/logout` | POST | 🔑 | 任意登录用户 | 本人 | 允许 | - | 🟢 |
| `/auth/me` | GET | 🔑 | 任意登录用户 | 本人 | 允许 | - | 🟢 |
| `/auth/permissions` | GET | 🔑 | 任意登录用户 | 本人 | 允许 | - | 🟢 |
| `/health*` | GET | 🔓 | - | - | 允许 | - | 🟢 |
| `/school/roles/options` | GET | 🔑 | `school_admin` | 本租户 | 允许 | - | 🟢 前缀 |
| `/school/admin-users`（读） | GET | 🔑 | `school_admin` | 本租户 | 允许 | - | 🟢 |
| `/school/admin-users`（写） | POST/PUT | 🔑 | `school_admin` | 本租户 | 允许 | ✅ | 🟢 |
| `/school/staff-users`（读/写/`/{id}/roles`） | GET/POST/PUT | 🔑 | `school_admin` | 本租户 | 允许 | 写✅ | 🟢 |
| `/school/student-users`（读/写） | GET/POST/PUT | 🔑 | `school_admin` | 本租户 | 允许 | 写✅ | 🟢 |
| `/school/profile/current` | GET/PUT | 🔑 | `school_admin` | 本校 | 允许 | PUT✅ | 🔴 仅需登录 |
| `/school/teaching-orgs`（读） | GET | 🔑 | 登录 | 本租户 | 允许 | - | 🔴 无角色 |
| `/school/teaching-orgs`（写） | POST/PUT/DELETE | 🔑 | `teaching_admin`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/school/service-orgs`（读） | GET | 🔑 | 登录 | 本租户 | 允许 | - | 🔴 无角色 |
| `/school/service-orgs`（写） | POST/PUT/DELETE | 🔑 | `service_admin`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/school/classes`（读） | GET | 🔑 | 登录 | 本租户 | 允许 | - | 🔴 无角色 |
| `/school/classes`（写） | POST/PUT/DELETE | 🔑 | `teaching_admin`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/school/courses`（读/`options`） | GET | 🔑 | 登录 | 本租户 | 允许 | - | 🔴 无角色 |
| `/school/courses`（写/`/{id}/teachers`） | POST/PUT/DELETE | 🔑 | `teaching_admin`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/school/service-items`（读） | GET | 🔑 | 登录 | 本租户 | 允许 | - | 🔴 无角色 |
| `/school/service-items`（写） | POST/PUT/DELETE | 🔑 | `service_admin`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/evaluation/forms`（读） | GET | 🔑 | `form_publisher`/`school_admin` | 本租户 | 允许 | - | 🔴 无角色 |
| `/evaluation/forms`（新增/编辑/复制/submit-audit） | POST/PUT | 🔑 | `form_publisher`/`school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/evaluation/forms`（删除） | DELETE | 🔑 | `school_admin` | 本租户 | 允许 | ✅ | 🔴 无角色 |
| `/evaluation/forms/{formId}/questions` | GET/PUT | 🔑 | `form_publisher`/`school_admin` | 本租户 | 允许 | PUT✅ | 🔴 无角色 |
| `/evaluation/forms/{formId}/window` | GET/PUT | 🔑 | `form_publisher`/`school_admin` | 本租户 | 允许 | PUT✅ | 🔴 无角色 |
| `/evaluation/audits`（读） | GET | 🔑 | `school_admin` | 本租户 | 允许 | - | 🟢 前缀 |
| `/evaluation/audits/{id}/approve`、`/reject` | POST | 🔑 | `school_admin` | 本租户 | 允许 | ✅ | 🟢 前缀 |
| `/student/evaluation/tasks` | GET | 🔑 | `student` | 本人可见 | 只读 | - | 🟢 前缀 |
| `/student/evaluation/submissions`（提交） | POST | 🔑 | `student` | 本人 | 禁止代提交 | ✅ | 🟢 |
| `/student/evaluation/submissions`（读） | GET | 🔑 | `student` | 本人 | 只读 | - | 🟢 |
| `/files/upload` | POST | 🔑 | 登录 | 本租户 | 允许 | ✅ | 🟢 |
| `/files/{id}`、`/download`、`DELETE` | GET/DELETE | 🔑 | 登录 + 归属 | 本租户 | 允许 | DELETE✅ | 🟡 |
| `/files/{id}/preview` | GET | 🔑 | 见 §8.5 | 本租户 | 允许 | - | 🟢 已收紧 |

> `/platform/*`（tenants/onboarding/plans/roles/audit）**尚未实现**，权限矩阵待开发时补充（§10 标 `未实现`）。
> 🔴 **整改总则**："学校业务 CRUD / 评价表单管理"当前缺接口级角色/权限校验，需在 v1.0 前于 Controller/方法补齐 `@SaCheckRole`/`@SaCheckPermission`，并将 `SaTokenConfigure` 前缀白名单收敛到与本矩阵一致。

---

## 10. 接口总清单（A 部分）

> 路径均为相对 context-path `/api` 的实际路径；"状态"列：`已实现`/`未实现`。分页接口入参目标为 `page/pageSize`（现为 `pageNum`，见 §2.3）。所有列表均支持 `keyword`/`status` 类过滤（具体见代码）。

### 10.1 认证 `/auth`

| 方法 | 路径 | 说明 | 分页 | 审计 | 状态 |
| --- | --- | --- | - | - | --- |
| POST | `/auth/login` | 登录 | | | 已实现 |
| POST | `/auth/logout` | 登出 | | | 已实现 |
| GET | `/auth/me` | 当前用户信息 | | | 已实现 |
| GET | `/auth/permissions` | 权限信息 | | | 已实现 |
| GET | `/auth/routes` | 前端路由（预留） | | | 已实现 |
| GET | `/auth/captcha` | 验证码（预留） | | | 已实现 |

### 10.2 平台 `/platform`（**整体未实现**）

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET/POST/PUT | `/platform/tenants/*` | 租户管理 | 未实现 |
| GET/POST | `/platform/onboarding/*`、`/{id}/approve` | 入驻审核 | 未实现 |
| GET/POST/PUT | `/platform/plans/*` | 套餐管理 | 未实现 |
| GET/POST/PUT | `/platform/roles/*` | 平台角色 | 未实现 |
| GET | `/platform/audit/*` | 平台审计日志 | 未实现 |

### 10.3 学校 `/school`

| 方法 | 路径 | 说明 | 分页 | 状态 |
| --- | --- | --- | --- | --- |
| GET/PUT | `/school/profile/current` | 当前学校资料 | | 已实现 |
| GET | `/school/roles/options` | 可分配角色选项 | | 已实现 |
| GET | `/school/teaching-orgs`、`/tree` | 教学组织列表/树 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/school/teaching-orgs`、`/{id}` | 教学组织增删改 | | 已实现 |
| GET | `/school/service-orgs` | 服务组织 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/school/service-orgs`、`/{id}` | 服务组织增删改 | | 已实现 |
| GET | `/school/classes`、`/options` | 班级列表/选项 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/school/classes`、`/{id}` | 班级增删改 | | 已实现 |
| GET | `/school/courses`、`/{id}`、`/options` | 课程 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/school/courses`、`/{id}`、`/{id}/teachers` | 课程增删改/教师维护 | | 已实现 |
| GET | `/school/service-items`、`/{id}`、`/options` | 服务项目 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/school/service-items`、`/{id}` | 服务项目增删改 | | 已实现 |
| GET | `/school/admin-users`、`/{id}` | 管理员列表/详情 | ✅分页 | 已实现 |
| POST/PUT | `/school/admin-users`、`/{id}`、`/{id}/status`、`/{id}/reset-password` | 管理员维护 | | 已实现 |
| GET | `/school/staff-users`、`/{id}` | 教职工列表/详情 | ✅分页 | 已实现 |
| POST/PUT | `/school/staff-users`、`/{id}`、`/{id}/roles`、`/{id}/status`、`/{id}/reset-password` | 教职工维护 | | 已实现 |
| GET | `/school/student-users`、`/{id}` | 学生列表/详情 | ✅分页 | 已实现 |
| POST/PUT | `/school/student-users`、`/{id}`、`/{id}/status`、`/{id}/reset-password` | 学生维护 | | 已实现 |

### 10.4 评价 `/evaluation`、学生 `/student`、文件 `/files`、健康 `/health`

| 方法 | 路径 | 说明 | 分页 | 状态 |
| --- | --- | --- | --- | --- |
| GET | `/evaluation/forms`、`/{id}` | 表单列表/详情 | ✅分页 | 已实现 |
| POST/PUT/DELETE | `/evaluation/forms`、`/{id}`、`/{id}/copy`、`/{id}/submit-audit` | 表单维护 | | 已实现 |
| GET/PUT | `/evaluation/forms/{formId}/questions` | 题目配置 | | 已实现 |
| GET/PUT | `/evaluation/forms/{formId}/window` | 窗口配置 | | 已实现 |
| GET | `/evaluation/audits`、`/{id}` | 审核列表/详情 | ✅分页 | 已实现 |
| POST | `/evaluation/audits/{id}/approve`、`/reject` | 审核通过/驳回 | | 已实现 |
| GET | `/student/evaluation/tasks`、`/{formId}` | 学生任务列表/详情 | ✅分页 | 已实现 |
| POST | `/student/evaluation/submissions` | 提交评价 | | 幂等待补 |
| GET | `/student/evaluation/submissions`、`/{id}` | 我的提交/详情 | ✅分页 | 已实现 |
| POST | `/files/upload` | 上传 | | 已实现 |
| GET | `/files/{id}`、`/{id}/download`、`/{id}/preview` | 元数据/下载/预览 | | 已实现 |
| DELETE | `/files/{id}` | 删除 | | 已实现 |
| GET | `/health`、`/health/db`、`/health/redis` | 健康检查 | | 已实现 |

> **幂等与审计标注**：写操作默认记录审计（`@OperationLog`）；**幂等性**：当前仅"学生提交评价"通过唯一键防重（冲突返回 409），其余写接口未声明幂等令牌，如需严格幂等需在 v1.0 前补充（如导入类接口的 `requestId`）。

---

## 11. 重点接口示例

> 以下示例展示**目标规范**（含 `timestamp`、对齐的 HTTP 状态、`page` 分页、`roleType`）。代码尚未完全对齐处见 §1~§9 的 🔴 标注。

### 11.1 登录 `POST /api/auth/login`

请求：
```json
{ "username": "student_zhang", "password": "123456" }
```
成功 `200`：
```json
{
  "code": 200, "message": "success", "timestamp": "2026-07-07 10:00:00",
  "data": {
    "tokenName": "satoken", "token": "uuid-...", "expiresIn": 86400,
    "userId": 103, "username": "student_zhang", "realName": "张三",
    "roleType": "student", "roles": ["student"], "permissions": ["eval:submit"],
    "tenantId": 2, "schoolId": 2, "mustChangePassword": false, "avatarUrl": null
  }
}
```
失败（账号停用）`HTTP 422`：
```json
{ "code": 422, "message": "账号已被停用", "data": null, "errKey": "USER_DISABLED", "timestamp": "2026-07-07 10:00:00" }
```

### 11.2 分页列表 `GET /api/school/courses?page=1&pageSize=20`

成功 `200`：
```json
{
  "code": 200, "message": "success", "timestamp": "2026-07-07 10:00:00",
  "data": { "records": [ { "id": 1, "courseName": "高等数学", "status": "active" } ],
            "total": 1, "page": 1, "pageSize": 20 }
}
```

### 11.3 提交评价 `POST /api/student/evaluation/submissions`

请求：
```json
{
  "formId": 1, "windowId": 1, "targetType": "service_item", "targetId": 5,
  "answers": [ { "questionId": 11, "score": 5, "comment": "很好" } ]
}
```
成功 `200`：
```json
{ "code": 200, "message": "success", "timestamp": "2026-07-07 10:00:00",
  "data": { "submissionId": 100, "submittedAt": "2026-07-07 10:00:00", "totalScore": 5, "averageScore": 5.0 } }
```
失败（重复提交）`HTTP 409`：
```json
{ "code": 409, "message": "您已提交过该评价", "data": null, "errKey": "DUPLICATE_SUBMISSION", "timestamp": "2026-07-07 10:00:00" }
```

### 11.4 未登录 `GET /api/auth/me`（无 token）`HTTP 401`
```json
{ "code": 401, "message": "未登录或登录已过期，请重新登录", "data": null, "errKey": "UNAUTHORIZED", "timestamp": "2026-07-07 10:00:00" }
```

### 11.5 跨租户访问 `GET /api/school/courses/999`（资源属他租户）`HTTP 403`
```json
{ "code": 403, "message": "没有操作权限", "data": null, "errKey": "TENANT_FORBIDDEN", "timestamp": "2026-07-07 10:00:00" }
```
> 注：不返回 404，避免探测他租户资源是否存在（§5.7）。

### 11.6 上传文件 `POST /api/files/upload`（`multipart/form-data`：`file`、`bizType=avatar`）`200`
```json
{ "code": 200, "message": "success", "timestamp": "2026-07-07 10:00:00",
  "data": { "id": 55, "fileName": "me.png", "mimeType": "image/png", "url": "/upload/files/avatar/xxx.png",
            "size": 20480, "bizType": "avatar", "bizId": null, "uploaderId": 103,
            "tenantId": 2, "schoolId": 2, "createdAt": "2026-07-07 10:00:00", "updatedAt": "2026-07-07 10:00:00" } }
```

---

## 12. 版本演进与冻结门槛

- **当前 v0.3 已冻结公共基线**。本次冻结仅覆盖第一阶段的公共协议，不等同于 A/B 产品功能完成。四类角色、两个租户的全量业务联调通过后，另行升级为 v1.0 产品冻结版。
- 冻结后任何破坏性变更（字段重命名/删除/语义变化）必须评审并升版本号。
- 兼容策略：字段重命名（`pageNum→page`、`userType→roleType`）迁移期可双写，前端切换完成后删除旧字段（最迟随 v1.0 发布移除）。
- 分歧裁定：以本文档 + 已落地并通过对齐的后端实现为准。

## 13. 第一阶段冻结核验与后续待办

> “第一阶段冻结核验”记录公共协议已落地的证据；“后续待办”不阻塞本阶段提交，但阻塞 v1.0 产品冻结。

**第一阶段冻结核验**
- [x] `R` 新增 `timestamp`，`ok/fail` 均填充（`yyyy-MM-dd HH:mm:ss`）。
- [x] 新增全局 `JacksonConfig`，使 `LocalDateTime` 序列化为 `yyyy-MM-dd HH:mm:ss`。

**HTTP 状态码对齐（§1.2/§1.4）**
- [x] `SaTokenExceptionHandler` 的 401/403 返回真实 HTTP 状态（不再 HTTP 200）。
- [x] `GlobalExceptionHandler` 对 `BusinessException` 按 code 映射 HTTP 状态（`ResponseEntity`）。
- [x] 前端拦截器按“先看 HTTP status，再看业务 code”处理。

**分页（§2）**
- [x] 新增 `PageQuery`（`page/pageSize`），分页 Controller 接受正式 `page`，兼容 `pageNum`。
- [x] `PageResult.pageNum → page`（保留 `totalPages`），过渡期双写 `page`+`pageNum`。

**用户字段（§3）**
- [x] `LoginResponse`/`CurrentUserVO`：`userType → roleType`（过渡期双写）。
- [x] `/school/roles/options` 入参 `userType → roleType`。
- [x] 前端建立公共字段适配，路由优先使用 `roleType`。

**状态值（§5）**
- [x] 用户状态支持 `locked`，`ChangeUserStatusDTO` 去除 `enabled` 别名。

**错误码（§6）**
- [x] `ErrorCode` 补 `CONFLICT(409)`/`UNPROCESSABLE(422)`/`SERVICE_UNAVAILABLE(503)`，停用 `600`。
- [x] `R`/`BusinessException` 支持 `errKey` 字段；公共异常映射支持显式 `409/422` + `errKey`。

**第一阶段验证证据**
- [x] `campus-admin` 的 `PublicContractTest` 覆盖响应时间戳、错误标识、分页兼容、异常 HTTP 映射。
- [x] 后端 `mvn test`、`mvn -DskipTests compile` 通过；前端 `npm run build` 通过；`git diff --check` 通过。

**租户安全（§7）**
- [x] 受保护接口移除 `DEFAULT_TENANT_ID` 静默回退，上下文缺失→`401`。
- [ ] 按 ID 的查/改/删统一 `id + tenantId` 归属校验，跨租户统一 `403` 且不泄露存在性。
- [ ] 评估启用 `TenantLineInnerInterceptor` 自动注入租户条件。

**附件（§8）**
- [x] `preview` 收紧鉴权（登录 + 归属，或按 `bizType` 公开白名单）。
- [x] `MaxUploadSizeExceededException` 纳入全局处理→`422 FILE_TOO_LARGE`。
- [ ] 补充 MIME/魔数校验与临时文件回收定时任务方案。

**权限矩阵（§9）**
- [ ] 学校业务 CRUD / 评价表单管理补齐接口级 `@SaCheckRole`/`@SaCheckPermission`。
- [ ] `SaTokenConfigure` 前缀白名单收敛至与 §9 矩阵一致。

**接口清单（§10/§11）**
- [ ] `/platform/*` 系列接口实现后补充清单与权限矩阵。
- [ ] 按 §11 示例校验全量接口的字段/时间/错误响应一致性。
