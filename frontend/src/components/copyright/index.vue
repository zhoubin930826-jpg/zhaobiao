<template>
    <div class="footer">
        <span>Copyright © 2021 西安智联盾科技有限公司</span>
        <a class="link" href="https://beian.miit.gov.cn" target="_blank">陕ICP备2021005666号-1</a>
        <a class="link" :href="publicSecurityUrl" target="_blank">陕公网安备{{ publicSecurityNumber }}号</a>
    </div>
</template>
<script>
    export default {
        name: 'i-copyright',
        data () {
            return {
                publicSecurityNumber: '61019002001746'
            }
        },
        computed: {
            publicSecurityUrl () {
                return `http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=${this.publicSecurityNumber}`
            }
        }
    }
</script>

<style lang="less" scoped>
.footer{
    text-align: center;
    padding: 1rem 0;
    .link {
        color: #515a6e;
        margin-left: 10px;
        &:hover {
            color: #000;
        }
    }
}
</style>
