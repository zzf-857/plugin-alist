# Halo AList 存储连接与反代修复指南

此文档记录了针对 Halo 博客连接 AList 外部存储失败的排查与修复全过程，可作为后期运维与重新配置时的参考。

---

## 🔍 问题排查与分析

1. **站点 IP 拼写错误**：
   原配置中将服务器公网 IP `8.148.202.157` 误填为了 `0.110.202.157`，因 `0.` 开头为无效保留 IP，导致连接直接被阻断。
2. **管理员密码不匹配**：
   在服务器本地测试 AList 的 `/api/auth/login` 接口时，提示 `username or password is incorrect`，表明 AList 内的实际密码与提供的配置密码不一致，需要进行重置。
3. **安全组限制与网络环境**：
   * AList 的默认运行端口是 `5244`，但出于安全考虑，并未对公网开放。
   * Halo 容器与 AList 容器均在 Docker 内网 `1panel-network` 网桥下，彼此能直连。
   * OpenResty (Nginx) 容器以 `host` 模式运行在宿主机网络。

---

## 🛠️ 执行的修复步骤

### 步骤 1：重置 AList 管理员密码
在服务器宿主机上以 root 身份执行命令，将 `admin` 的密码更新为指定的 `zzf210812`：
```bash
docker exec 1Panel-alist-s2uD ./alist admin set zzf210812
```
* **API 验证**：经 `curl` 测试登录接口，AList 响应了 `200 success`，证明密码修改成功且 API 状态正常。

### 步骤 2：配置 Nginx (OpenResty) 对外下载路径反向代理
由于 OpenResty 以 `host` 网络模式运行，我们可以将其配置直接反代到宿主机的 AList 端口（`127.0.0.1:5244`）。

修改 `/opt/1panel/apps/openresty/openresty/conf/conf.d/blog.zengxiansheng.top.conf` 文件，在 `server` 块中加入针对 `/d/` 路径（AList 直链路径）的反代规则：
```nginx
    location /d/ {
        proxy_pass http://127.0.0.1:5244;
    }
```
* **测试与生效**：
  ```bash
  docker exec 1Panel-openresty-ueq8 openresty -t
  docker exec 1Panel-openresty-ueq8 openresty -s reload
  ```
* **验证**：本地发起 `https://blog.zengxiansheng.top/d/` HTTPS 请求成功，外网可以在不暴露 `5244` 端口的前提下，安全地以 HTTPS 协议读取 AList 中存储的附件素材。

---

## 📝 Halo 后台配置规范表

修复后，在 Halo 博客控制台的“存储策略”中按以下规范填写即可成功互通：

| 表单配置项 | 填写的值 | 作用 |
| :--- | :--- | :--- |
| **站点 (site)** | `http://1Panel-alist-s2uD:5244` | 指向 AList 的 Docker 内部容器地址，实现极速且安全的内网通信。 |
| **附件访问前缀** | `https://blog.zengxiansheng.top/d` | 确保博客引用的图片，外网用户可通过 Nginx 反代顺畅加载。 |
| **挂载路径** | `/123网盘` (或其他) | 对应的 AList 虚拟存储桶路径。 |
| **密钥 (Secret)** | 创建一个包含 `username: admin` 与 `password: zzf210812` 的 Secret | 用于 Halo 插件与 AList 通信进行鉴权。 |
