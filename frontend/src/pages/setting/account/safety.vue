<template>
  <div class="setting-account-safety">
    <h2>安全设置</h2>
    <div class="ivu-mt ivu-pl ivu-pr">
      <Row type="flex">
        <Col v-bind="grid2">
          <Form :model="formInline" label-position="left" :label-width="100">
            <FormItem label="原密码">
              <Input v-model="formInline.old_password" type="password"></Input>
            </FormItem>
            <FormItem label="新密码">
              <Input v-model="formInline.new_password" type="password"></Input>
            </FormItem>
            <FormItem label="新密码确认">
              <Input v-model="formInline.new_password1" type="password"></Input>
            </FormItem>
            <FormItem>
              <Button type="primary" @click="handleSubmit">确认更改</Button>
            </FormItem>
          </Form>
        </Col>
      </Row>
    </div>
  </div>
</template>
<script>
    import { Updatepwd } from '@/api/account'
    export default {
        data () {
            return {
                formInline: {
                    old_password: '',
                    new_password: '',
                    new_password1: ''
                },
                grid2: {
                    xl: {
                        span: 12,
                        order: 1
                    },
                    lg: {
                        span: 24,
                        order: 2
                    },
                    md: {
                        span: 24,
                        order: 2
                    },
                    sm: {
                        span: 24,
                        order: 2
                    },
                    xs: {
                        span: 24,
                        order: 2
                    }
                }
            };
        },
        methods: {
            handleSubmit () {
                if (this.formInline.new_password !== this.formInline.new_password1) {
                    this.$Message.error('两次输入密码不一致，请重新输入');
                    return;
                }
                Updatepwd({
                    newPassword: this.formInline.new_password,
                    confirmPassword: this.formInline.new_password1
                }).then(() => {
                    this.$Message.success('更改成功');
                });
                // console.log(this.formInline)
            }
        }
    };
</script>
<style lang="less" scoped>
.setting-account-safety {
  .ivu-col {
    p {
      margin-top: 8px;
      color: #808695;
    }
  }
}
</style>
