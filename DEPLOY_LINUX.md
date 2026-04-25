# Linux 服务器部署教程

这份文档用于把当前项目的后端部署到一台全新的 Linux 云服务器上。

内容包括：

- 安装 Docker
- 使用 Docker 启动 MySQL
- 安装 Java 8
- 上传并启动 Spring Boot 后端
- 将后端配置为 `systemd` 服务

## 1. 前提说明

本文默认你使用的是：

- 服务器系统为 `Ubuntu 22.04 LTS` 或 `Ubuntu 24.04 LTS`
- 你可以使用带 `sudo` 权限的账号登录服务器
- 服务器架构为 `x86_64`
- 要部署的后端就是当前仓库里的这个项目

如果你的服务器是 `CentOS`、`Rocky Linux`、`Alibaba Cloud Linux` 或其他发行版，这份文档需要做相应调整。

## 2. 当前项目运行信息

当前后端的运行特征如下：

- 框架：`Spring Boot 2.7.x`
- Java 版本：`Java 8`
- 默认 HTTP 端口：`8080`
- 数据库：`MySQL 8`
- 默认数据库名：`zhaobiao_admin`
- 打包后的 JAR 文件名：

```text
target/zhaobiao-admin-0.0.2-SNAPSHOT.jar
```

后端运行时会读取这些环境变量：

```text
SPRING_PROFILES_ACTIVE
MYSQL_HOST
MYSQL_PORT
MYSQL_DB
MYSQL_USER
MYSQL_PASSWORD
APP_JWT_SECRET
APP_BOOTSTRAP_ADMIN_PASSWORD
```

## 3. 登录服务器

在你本地电脑上执行：

```bash
ssh your_user@your_server_ip
```

示例：

```bash
ssh ubuntu@123.123.123.123
```

## 4. 更新系统并安装基础工具

在服务器上执行：

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release unzip
```

## 5. 安装 Docker

下面的安装方式遵循 Docker 官方的 Ubuntu 安装方法。

### 5.1 卸载旧版 Docker 相关包

如果服务器之前装过旧版 Docker，先执行：

```bash
for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; do
  sudo apt-get remove -y $pkg
done
```

### 5.2 添加 Docker 官方 GPG 密钥

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```

### 5.3 添加 Docker 软件源

```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### 5.4 安装 Docker Engine

```bash
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 5.5 设置 Docker 开机自启并立即启动

```bash
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker
```

如果你希望后续执行 Docker 命令时不用每次都加 `sudo`，可以执行：

```bash
sudo usermod -aG docker $USER
```

执行后退出一次登录，再重新登录：

```bash
exit
ssh your_user@your_server_ip
```

### 5.6 验证 Docker 是否安装成功

```bash
docker --version
docker compose version
```

## 6. 使用 Docker 启动 MySQL 8

因为这台服务器是全新的，同时我们已经安装了 Docker，所以数据库直接用 Docker 跑是当前项目最简单、也最方便维护的方案。

### 6.1 创建持久化数据卷

```bash
docker volume create zhaobiao-mysql-data
```

### 6.2 启动 MySQL 容器

请把下面的 `YourRootPassword123!` 替换成你自己的强密码。

```bash
docker run -d \
  --name zhaobiao-mysql \
  --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=YourRootPassword123! \
  -e MYSQL_DATABASE=zhaobiao_admin \
  -v zhaobiao-mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_general_ci
```

### 6.3 查看 MySQL 是否启动成功

```bash
docker ps
docker logs --tail 100 zhaobiao-mysql
```

### 6.4 进入容器连接 MySQL

```bash
docker exec -it zhaobiao-mysql mysql -uroot -p
```

输入密码后执行：

```sql
SHOW DATABASES;
```

正常情况下你会看到：

```text
zhaobiao_admin
```

### 6.5 创建应用专用数据库账号

不建议应用运行时直接使用 `root`，更推荐单独创建一个业务账号。

在 MySQL 中执行：

```sql
CREATE USER 'zb_app'@'%' IDENTIFIED BY 'YourAppPassword123!';
GRANT ALL PRIVILEGES ON zhaobiao_admin.* TO 'zb_app'@'%';
FLUSH PRIVILEGES;
```

建议：

- 后端连接数据库时使用 `zb_app`
- `root` 只保留给数据库管理使用

### 6.6 安全建议

如果你的后端和 MySQL 都部署在同一台服务器上，一般不要把 `3306` 对公网开放，除非你确实需要远程直连数据库。

推荐的云服务器安全组配置：

- 放行 `22`，用于 SSH 登录
- 放行 `8080`，用于访问后端
- 不要对公网开放 `3306`

## 7. 安装 Java 8

当前项目运行在 Java 8 上。对于较新的 Ubuntu 版本，安装 Temurin 8 通常是更稳妥的方案。

### 7.1 添加 Adoptium 软件源

```bash
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print $2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
```

### 7.2 安装 Temurin 8

```bash
sudo apt-get update
sudo apt-get install -y temurin-8-jdk
```

### 7.3 验证 Java 是否安装成功

```bash
java -version
javac -version
```

预期主版本为：

```text
1.8
```

### 7.4 确认 JAVA_HOME

```bash
readlink -f /usr/bin/java
```

常见输出类似：

```text
/usr/lib/jvm/temurin-8-jdk-amd64/bin/java
```

那么 `JAVA_HOME` 一般就是：

```text
/usr/lib/jvm/temurin-8-jdk-amd64
```

## 8. 在本地打包后端

这一步在你的本地开发电脑上执行，不是在服务器上执行：

```bash
mvn clean package -DskipTests
```

打包完成后，确认本地有这个文件：

```text
target/zhaobiao-admin-0.0.2-SNAPSHOT.jar
```

## 9. 上传后端 JAR 到服务器

先在服务器上创建部署目录：

```bash
sudo mkdir -p /opt/zhaobiao/app
sudo chown -R $USER:$USER /opt/zhaobiao
```

然后在本地电脑上执行上传命令：

```bash
scp target/zhaobiao-admin-0.0.2-SNAPSHOT.jar your_user@your_server_ip:/opt/zhaobiao/app/
```

示例：

```bash
scp target/zhaobiao-admin-0.0.2-SNAPSHOT.jar ubuntu@123.123.123.123:/opt/zhaobiao/app/
```

## 10. 创建后端环境变量文件

在服务器上执行：

```bash
cd /opt/zhaobiao/app
cat > app.env <<'EOF'
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DB=zhaobiao_admin
MYSQL_USER=zb_app
MYSQL_PASSWORD=YourAppPassword123!
APP_JWT_SECRET=ChangeThisToAVeryLongRandomSecretValue123456789
APP_BOOTSTRAP_ADMIN_PASSWORD=ChangeThisInitialAdminPassword123!
EOF
```

建议：

- 生产环境使用 `prod` profile 启动，这样会启用启动期安全校验
- `APP_JWT_SECRET` 要尽量长、足够随机，不要和其他项目共用
- `APP_BOOTSTRAP_ADMIN_PASSWORD` 只用于首次创建 `admin` 超级管理员
- `prod` 环境下如果仍使用 `root` 数据库账号，或者数据库密码仍为默认值 `root`，后端会直接启动失败
- 不要把 `app.env` 提交进 Git 仓库

## 11. 先手动启动一次后端

这一步的作用是先验证环境是否正常，再去配置 `systemd`。

在服务器上执行：

```bash
cd /opt/zhaobiao/app
set -a
source app.env
set +a
nohup java -jar zhaobiao-admin-0.0.2-SNAPSHOT.jar > app.log 2>&1 &
```

查看日志：

```bash
tail -f /opt/zhaobiao/app/app.log
```

如果启动成功，日志里通常会出现 Spring Boot 启动完成，以及 Tomcat 正在监听 `8080` 端口的提示。

## 12. 验证后端是否可用

在服务器本机执行：

```bash
curl http://127.0.0.1:8080/swagger-ui.html
```

如果你的云服务器安全组已经放行 `8080`，也可以直接在浏览器访问：

```text
http://your_server_ip:8080/
http://your_server_ip:8080/swagger-ui.html
```

默认超级管理员账号：

```text
username: admin
password: 首次启动时由 APP_BOOTSTRAP_ADMIN_PASSWORD 提供
```

## 13. 将后端配置为 systemd 服务

`nohup` 方式适合临时验证，但线上环境更推荐使用 `systemd` 托管。

### 13.1 先停止刚才手动启动的进程

查找进程：

```bash
ps -ef | grep zhaobiao-admin
```

或者：

```bash
ps -ef | grep java
```

然后停止旧进程：

```bash
kill -9 your_java_pid
```

### 13.2 创建 service 文件

```bash
sudo tee /etc/systemd/system/zhaobiao.service > /dev/null <<'EOF'
[Unit]
Description=Zhaobiao Spring Boot Backend
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
WorkingDirectory=/opt/zhaobiao/app
EnvironmentFile=/opt/zhaobiao/app/app.env
ExecStart=/usr/bin/java -jar /opt/zhaobiao/app/zhaobiao-admin-0.0.2-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF
```

说明：

- 当前先用 `User=root`，配置最简单
- 更稳妥的生产环境做法是创建单独的 Linux 用户，例如 `zhaobiao`

### 13.3 重新加载配置并启动服务

```bash
sudo systemctl daemon-reload
sudo systemctl enable zhaobiao
sudo systemctl start zhaobiao
sudo systemctl status zhaobiao
```

### 13.4 查看服务日志

```bash
sudo journalctl -u zhaobiao -f
```

## 14. 后端常用运维命令

### 启动后端

```bash
sudo systemctl start zhaobiao
```

### 停止后端

```bash
sudo systemctl stop zhaobiao
```

### 重启后端

```bash
sudo systemctl restart zhaobiao
```

### 查看后端状态

```bash
sudo systemctl status zhaobiao
```

### 查看后端日志

```bash
sudo journalctl -u zhaobiao -f
```

## 15. MySQL Docker 常用命令

### 启动 MySQL 容器

```bash
docker start zhaobiao-mysql
```

### 停止 MySQL 容器

```bash
docker stop zhaobiao-mysql
```

### 查看 MySQL 容器日志

```bash
docker logs --tail 100 zhaobiao-mysql
```

### 进入 MySQL 容器

```bash
docker exec -it zhaobiao-mysql bash
```

### 登录 MySQL

```bash
docker exec -it zhaobiao-mysql mysql -uroot -p
```

## 16. 云服务器安全组建议

建议对外开放的端口：

- `22`，用于 SSH
- `8080`，用于后端测试访问

不建议直接对公网开放：

- `3306`，MySQL 数据库端口

如果后续你会再加 Nginx：

- 开放 `80`
- 开放 `443`
- 可以考虑关闭公网 `8080`

## 17. 常见问题排查

### 17.1 后端启动失败，提示无法连接 MySQL

重点检查：

- `zhaobiao-mysql` 容器是否正在运行
- `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`MYSQL_USER`、`MYSQL_PASSWORD` 是否正确
- 是否已经创建了 `zb_app`
- `app.env` 中填写的密码是否正确

常用排查命令：

```bash
docker ps
docker logs --tail 100 zhaobiao-mysql
sudo journalctl -u zhaobiao -f
```

### 17.2 浏览器无法访问 8080

重点检查：

- 后端是否已经启动
- Ubuntu 防火墙是否拦截了端口
- 云服务器安全组是否已经放行 `8080`

如果你启用了 `ufw`，可以执行：

```bash
sudo ufw allow 8080/tcp
```

### 17.3 提示 `java: command not found`

检查：

```bash
java -version
which java
```

如果需要重新安装：

```bash
sudo apt-get install -y temurin-8-jdk
```

### 17.4 无法使用 Docker 命令

检查：

```bash
docker --version
sudo systemctl status docker
```

如果当前用户没有 Docker 权限：

```bash
sudo usermod -aG docker $USER
```

然后重新登录一次。

## 18. 下一步建议

当后端运行稳定之后，下一步更推荐补上这些部署能力：

1. 增加 `Nginx` 反向代理
2. 绑定域名
3. 使用 Let's Encrypt 配置 HTTPS
4. 把外部访问统一收敛到 `80/443`
5. 全面替换掉数据库 `root` 直连，改为最小权限业务账号
6. 增加日志轮转和数据库定时备份

## 19. 官方参考资料

本文中的安装步骤主要参考了以下官方文档：

- Docker Engine for Ubuntu：
  - https://docs.docker.com/engine/install/ubuntu/
- MySQL 官方 Docker 镜像：
  - https://hub.docker.com/_/mysql
- Adoptium Temurin：
  - https://adoptium.net/
