# Linux Server Deployment Guide

This guide is for deploying the current backend of this project to a brand-new Linux cloud server.

It covers:

- Installing Docker
- Starting MySQL with Docker
- Installing Java 8
- Uploading and starting the Spring Boot backend
- Configuring the backend as a `systemd` service

## 1. Assumptions

This guide assumes:

- The server OS is `Ubuntu 22.04 LTS` or `Ubuntu 24.04 LTS`
- You can log in with a user that has `sudo` privileges
- The server architecture is `x86_64`
- The backend runtime is the current project in this repository

If your server is `CentOS`, `Rocky Linux`, `Alibaba Cloud Linux`, or another distribution, this guide should be adjusted accordingly.

## 2. Project Runtime Information

Current backend characteristics:

- Framework: `Spring Boot 2.7.x`
- Java version: `Java 8`
- Default HTTP port: `8080`
- Database: `MySQL 8`
- Default database name: `zhaobiao_admin`
- Built JAR name:

```text
target/zhaobiao-admin-0.0.1-SNAPSHOT.jar
```

The backend reads these environment variables at runtime:

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DB
MYSQL_USER
MYSQL_PASSWORD
APP_JWT_SECRET
```

## 3. Connect to the Server

From your local machine:

```bash
ssh your_user@your_server_ip
```

Example:

```bash
ssh ubuntu@123.123.123.123
```

## 4. Update the Server and Install Basic Tools

Run on the server:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release unzip
```

## 5. Install Docker

The following installation method follows Docker's official Ubuntu installation approach.

### 5.1 Remove old Docker packages if they exist

```bash
for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; do
  sudo apt-get remove -y $pkg
done
```

### 5.2 Add Docker's official GPG key

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```

### 5.3 Add Docker repository

```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### 5.4 Install Docker Engine

```bash
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 5.5 Enable and start Docker

```bash
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker
```

If you want to use Docker without typing `sudo` every time:

```bash
sudo usermod -aG docker $USER
```

Then log out and log back in once:

```bash
exit
ssh your_user@your_server_ip
```

### 5.6 Verify Docker

```bash
docker --version
docker compose version
```

## 6. Start MySQL 8 with Docker

Because the server is brand new and Docker is already installed, using Docker to run MySQL is the simplest and most maintainable approach for this project.

### 6.1 Create a persistent volume

```bash
docker volume create zhaobiao-mysql-data
```

### 6.2 Start the MySQL container

Please replace `YourRootPassword123!` with your own strong password.

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

### 6.3 Check whether MySQL started successfully

```bash
docker ps
docker logs --tail 100 zhaobiao-mysql
```

### 6.4 Connect to MySQL inside the container

```bash
docker exec -it zhaobiao-mysql mysql -uroot -p
```

After entering the password, run:

```sql
SHOW DATABASES;
```

You should see:

```text
zhaobiao_admin
```

### 6.5 Create a dedicated application user

Using `root` directly is not recommended for application runtime. Create a dedicated user instead.

Inside MySQL:

```sql
CREATE USER 'zb_app'@'%' IDENTIFIED BY 'YourAppPassword123!';
GRANT ALL PRIVILEGES ON zhaobiao_admin.* TO 'zb_app'@'%';
FLUSH PRIVILEGES;
```

Recommendation:

- Use `zb_app` as the application database user
- Keep `root` only for database administration

### 6.6 Security advice

If your backend and MySQL are on the same server, do not expose `3306` publicly in the cloud security group unless remote database access is truly needed.

Recommended cloud security group settings:

- Allow `22` for SSH
- Allow `8080` for the backend
- Do not open `3306` to the public internet

## 7. Install Java 8

This project runs on Java 8. On newer Ubuntu versions, installing Temurin 8 is usually the most stable route.

### 7.1 Add the Adoptium repository

```bash
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print $2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
```

### 7.2 Install Temurin 8

```bash
sudo apt-get update
sudo apt-get install -y temurin-8-jdk
```

### 7.3 Verify Java

```bash
java -version
javac -version
```

Expected major version:

```text
1.8
```

### 7.4 Confirm JAVA_HOME

```bash
readlink -f /usr/bin/java
```

Typical output might be similar to:

```text
/usr/lib/jvm/temurin-8-jdk-amd64/bin/java
```

Then `JAVA_HOME` is usually:

```text
/usr/lib/jvm/temurin-8-jdk-amd64
```

## 8. Build the Backend on Your Local Machine

Run this on your local development machine, not on the server:

```bash
mvn clean package -DskipTests
```

After packaging, confirm this file exists locally:

```text
target/zhaobiao-admin-0.0.1-SNAPSHOT.jar
```

## 9. Upload the Backend JAR to the Server

Create the deployment directory on the server:

```bash
sudo mkdir -p /opt/zhaobiao/app
sudo chown -R $USER:$USER /opt/zhaobiao
```

From your local machine, upload the JAR:

```bash
scp target/zhaobiao-admin-0.0.1-SNAPSHOT.jar your_user@your_server_ip:/opt/zhaobiao/app/
```

Example:

```bash
scp target/zhaobiao-admin-0.0.1-SNAPSHOT.jar ubuntu@123.123.123.123:/opt/zhaobiao/app/
```

## 10. Create the Backend Environment File

On the server:

```bash
cd /opt/zhaobiao/app
cat > app.env <<'EOF'
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DB=zhaobiao_admin
MYSQL_USER=zb_app
MYSQL_PASSWORD=YourAppPassword123!
APP_JWT_SECRET=ChangeThisToAVeryLongRandomSecretValue123456789
EOF
```

Recommendation:

- `APP_JWT_SECRET` should be long, random, and not reused elsewhere
- Do not commit `app.env` into Git

## 11. Start the Backend Manually Once

This step is for validation before configuring `systemd`.

On the server:

```bash
cd /opt/zhaobiao/app
set -a
source app.env
set +a
nohup java -jar zhaobiao-admin-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

Check logs:

```bash
tail -f /opt/zhaobiao/app/app.log
```

If startup succeeds, look for logs indicating that Spring Boot has started and Tomcat is listening on port `8080`.

## 12. Verify the Backend

On the server:

```bash
curl http://127.0.0.1:8080/swagger-ui.html
```

If your cloud security group allows `8080`, you can also access from your browser:

```text
http://your_server_ip:8080/
http://your_server_ip:8080/swagger-ui.html
```

Default super admin account:

```text
username: admin
password: adminqwert
```

## 13. Configure the Backend as a systemd Service

Manual `nohup` startup is useful for testing, but production should use `systemd`.

### 13.1 Stop the manual process first

Find the process:

```bash
ps -ef | grep zhaobiao-admin
```

Or:

```bash
ps -ef | grep java
```

Then stop the old process:

```bash
kill -9 your_java_pid
```

### 13.2 Create the service file

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
ExecStart=/usr/bin/java -jar /opt/zhaobiao/app/zhaobiao-admin-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF
```

Note:

- `User=root` is the simplest setup for now
- A more secure production setup would create a dedicated Linux user such as `zhaobiao`

### 13.3 Reload and start the service

```bash
sudo systemctl daemon-reload
sudo systemctl enable zhaobiao
sudo systemctl start zhaobiao
sudo systemctl status zhaobiao
```

### 13.4 View service logs

```bash
sudo journalctl -u zhaobiao -f
```

## 14. Day-to-Day Backend Commands

### Start backend

```bash
sudo systemctl start zhaobiao
```

### Stop backend

```bash
sudo systemctl stop zhaobiao
```

### Restart backend

```bash
sudo systemctl restart zhaobiao
```

### Check backend status

```bash
sudo systemctl status zhaobiao
```

### View backend logs

```bash
sudo journalctl -u zhaobiao -f
```

## 15. Day-to-Day MySQL Docker Commands

### Start MySQL container

```bash
docker start zhaobiao-mysql
```

### Stop MySQL container

```bash
docker stop zhaobiao-mysql
```

### Check MySQL container logs

```bash
docker logs --tail 100 zhaobiao-mysql
```

### Enter MySQL container

```bash
docker exec -it zhaobiao-mysql bash
```

### Log in to MySQL

```bash
docker exec -it zhaobiao-mysql mysql -uroot -p
```

## 16. Cloud Security Group Recommendations

Recommended external ports:

- `22` for SSH
- `8080` for backend testing

Not recommended to expose publicly:

- `3306` MySQL

If you later add Nginx:

- Open `80`
- Open `443`
- Consider closing public `8080`

## 17. Common Problems

### 17.1 Backend fails to start because it cannot connect to MySQL

Check:

- Is `zhaobiao-mysql` running
- Are `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD` correct
- Did you create `zb_app`
- Is the password in `app.env` correct

Useful commands:

```bash
docker ps
docker logs --tail 100 zhaobiao-mysql
sudo journalctl -u zhaobiao -f
```

### 17.2 Port 8080 cannot be accessed from the browser

Check:

- Is the backend running
- Does Ubuntu firewall block the port
- Does the cloud provider security group allow `8080`

Example firewall command:

```bash
sudo ufw allow 8080/tcp
```

### 17.3 `java: command not found`

Check:

```bash
java -version
which java
```

If needed, reinstall:

```bash
sudo apt-get install -y temurin-8-jdk
```

### 17.4 Docker command not available

Check:

```bash
docker --version
sudo systemctl status docker
```

If your current user cannot run Docker:

```bash
sudo usermod -aG docker $USER
```

Then log in again.

## 18. Suggested Next Step

Once the backend is running normally, the next recommended deployment improvements are:

1. Add `Nginx` reverse proxy
2. Bind a domain name
3. Configure HTTPS with Let's Encrypt
4. Move the backend behind `80/443`
5. Replace database `root` usage everywhere with a least-privilege app account
6. Set up log rotation and scheduled database backups

## 19. Official References

The installation steps in this guide are based on the official documentation below:

- Docker Engine on Ubuntu:
  - https://docs.docker.com/engine/install/ubuntu/
- MySQL official Docker image:
  - https://hub.docker.com/_/mysql
- Adoptium Temurin packages:
  - https://adoptium.net/
