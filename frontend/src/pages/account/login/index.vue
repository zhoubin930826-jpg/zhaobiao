<template>
    <div class="page-account">
        <div v-if="showI18n" class="page-account-header">
            <i-header-i18n />
        </div>

        <img class="page-account-img" src="@/assets/images/login/bg.png" alt="">

        <div class="page-account-container">
            <h1 class="page-account-title">招投标管理系统后台</h1>

            <Form
                ref="loginForm"
                :model="form"
                :rules="rules"
                @keydown.enter.native="handleSubmit"
            >
                <FormItem prop="username">
                    <div class="form-item">
                        <i class="icon-username"></i>
                        <Input
                            v-model="form.username"
                            placeholder="请输入用户名"
                            maxlength="30"
                            :border="false"
                        />
                    </div>
                </FormItem>
                <FormItem prop="password">
                    <div class="form-item">
                        <i class="icon-password"></i>
                        <Input
                            type="password"
                            v-model="form.password"
                            placeholder="请输入密码"
                            maxlength="20"
                            :border="false"
                        />
                    </div>
                </FormItem>
                <FormItem>
                    <Checkbox v-model="rememberPwd">记住账号密码</Checkbox>
                </FormItem>
                <FormItem>
                    <Button @click="handleSubmit" type="primary" shape="circle" long>登 录</Button>
                </FormItem>
            </Form>
        </div>

        <div class="page-account-footer">
            <!-- <i-copyright /> -->
        </div>
    </div>
</template>

<script>
    import iCopyright from '@/components/copyright';
    import { mapActions } from 'vuex';
    import mixins from '../mixins';
    import { Base64 } from 'js-base64';

    export default {
        mixins: [mixins],
        components: { iCopyright },
        data () {
            return {
                form: {
                    username: '', // 用户名
                    password: '' // 密码
                },
                rememberPwd: false, // 是否记住账号密码
                rules: {
                    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
                    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
                }
            };
        },
        mounted () {
            const username = localStorage.getItem('username');
            const password = localStorage.getItem('password');
            if (username) {
                this.form.username = username;
                this.form.password = Base64.decode(password);
                this.rememberPwd = true;
            }
        },
        methods: {
            ...mapActions('admin/account', ['login']),
            // 登录
            handleSubmit () {
                this.$refs.loginForm.validate(valid => {
                    if (valid) {
                        this.login(this.form).then(() => {
                            // 重定向对象不存在则返回顶层路径
                            this.$router.replace(this.$route.query.redirect || '/');
                            this.setStorage();
                        });
                    }
                });
            },
            setStorage () {
                if (this.rememberPwd) {
                    localStorage.setItem('username', this.form.username);
                    localStorage.setItem('password', Base64.encode(this.form.password));
                } else {
                    localStorage.setItem('username', '');
                    localStorage.setItem('password', '');
                }
            }
        }
    };
</script>

<style lang="less" scoped></style>
