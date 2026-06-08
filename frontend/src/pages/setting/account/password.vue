<template>
  <div>
    <h2>修改密码</h2>
    <p class="hint">密码长度 6-32 位，修改成功后请使用新密码重新登录。</p>
    <Form
      ref="form"
      :model="data"
      :rules="rules"
      label-position="top"
      class="ivu-mt"
    >
      <Row type="flex" :gutter="48">
        <Col v-bind="grid2">
          <FormItem label="新密码" prop="newPassword">
            <Input
              v-model="data.newPassword"
              type="password"
              password
              placeholder="请输入新密码（6-32位）"
            />
          </FormItem>
          <FormItem label="确认新密码" prop="confirmPassword">
            <Input
              v-model="data.confirmPassword"
              type="password"
              password
              placeholder="请再次输入新密码"
            />
          </FormItem>
          <FormItem>
            <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
          </FormItem>
        </Col>
      </Row>
    </Form>
  </div>
</template>
<script>
    import { Updatepwd } from '@api/account';

    export default {
        data () {
            const validateConfirm = (rule, value, callback) => {
                if (!value) {
                    callback(new Error('请再次输入新密码'));
                    return;
                }
                if (value !== this.data.newPassword) {
                    callback(new Error('两次输入的新密码不一致'));
                    return;
                }
                callback();
            };
            return {
                submitting: false,
                grid2: {
                    xl: { span: 12, order: 1 },
                    lg: { span: 24, order: 2 },
                    md: { span: 24, order: 2 },
                    sm: { span: 24, order: 2 },
                    xs: { span: 24, order: 2 }
                },
                data: {
                    newPassword: '',
                    confirmPassword: ''
                },
                rules: {
                    newPassword: [
                        { required: true, message: '请输入新密码', trigger: 'blur' },
                        { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
                    ],
                    confirmPassword: [
                        { required: true, validator: validateConfirm, trigger: 'blur' }
                    ]
                }
            };
        },
        methods: {
            handleSubmit () {
                this.$refs.form.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    Updatepwd({
                        newPassword: this.data.newPassword,
                        confirmPassword: this.data.confirmPassword
                    }).then(() => {
                        this.$Message.success('密码修改成功');
                        this.data.newPassword = '';
                        this.data.confirmPassword = '';
                        this.$refs.form.resetFields();
                    }).finally(() => {
                        this.submitting = false;
                    });
                });
            }
        }
    };
</script>

<style lang="less" scoped>
.hint {
  margin: 0;
  color: #808695;
  font-size: 14px;
  line-height: 1.6;
}
</style>
