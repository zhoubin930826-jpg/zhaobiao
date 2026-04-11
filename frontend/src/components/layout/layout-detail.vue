<template>
    <div class="layout-detail">
        <!-- 标题部分 -->
        <div class="title-wrapper" :class="titleClass">
            <template v-if="!!title_name">
                <div v-if="back" class="title-back" @click="goBack">
                    <Icon type="ios-arrow-back" />
                    <span>返回</span>
                </div>
                <span class="title-name">{{ title_name }}</span>
                <slot name="header"></slot>
            </template>
            <template v-else>
                <slot name="title"></slot>
            </template>
        </div>

        <!-- 内容部分 -->
        <div class="content-wrapper">
            <slot name="content"></slot>
        </div>
    </div>
</template>

<script>
    import Setting from '@/setting';

    export default {
        name: 'LayoutDetail',
        props: {
            title_name: {
                type: String
            },
            back: {
                type: Boolean,
                default: true
            }
        },
        data () {
            return {};
        },
        computed: {
            titleClass () {
                return Setting.layout.tabs ? 'p-tb-16' : 'm-tb-16';
            }
        },
        methods: {
            // 返回
            goBack () {
                this.$router.go(-1);
            }
        }
    }
</script>

<style lang="less" scoped>
.layout-detail {
    .title-wrapper {
        display: flex;
        align-items: flex-end;
        .title-back {
            position: relative;
            display: flex;
            align-items: center;
            padding-right: 10px;
            margin-right: 10px;
            font-size: 14px;
            cursor: pointer;
            &:after {
                content: "";
                width: 1px;
                height: 18px;
                background: #B8C3D1;
                position: absolute;
                right: 0;
                top: 0;
            }
        }
        .title-name {
            line-height: 28px;
            font-size: 20px;
            font-weight: 600;
        }
    }
}
.p-tb-16 {
    padding-top: 16px;
    padding-bottom: 16px;
}
.m-tb-16 {
    margin-top: 16px;
    margin-bottom: 16px;
}
</style>
