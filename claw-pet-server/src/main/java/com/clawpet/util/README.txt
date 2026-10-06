# util/ — 工具类

存放通用工具方法，提供跨业务模块可复用的功能组件。

| 文件名 | 作用 |
|--------|------|
| CaptchaUtil.java | 算术验证码生成工具，生成加减法算术运算的验证码图片与正确结果 |
| UploadPathResolver.java | 上传目录解析器 — 按 代码位置 → `java.class.path` → `user.dir` 的顺序定位 `upload/`，再向上找含 `pom.xml` 的模块根 |

> 上传文件的**三级校验**（扩展名 / 大小 / Magic Number）不在这个包里，
> 它和上传接口绑在一起，写在 `controller/UploadController.java` 里。

> `UploadPathResolver` 解决的是「IDE 里跑和 `java -jar` 跑，图片落到不同目录」的问题：
> 早前用 `user.dir` 拼相对路径，从仓库根启动就会把图片落到 `<仓库根>/upload`，
> 表现为「上传成功但图片打不开」。改它之前，先把这两种启动方式的路径都验一遍。
