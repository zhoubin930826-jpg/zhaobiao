<template>
<!-- 触发BFC -->
<div style='margin: 20px 0;'>
  <Form  :model="formData" ref="ruleForm" :label-width="100">
    <Row>
      <Col :span="span" v-for="field in fields" :key="field.value">
        <FormItem :label="field.label" :prop="field.value">
          <slot v-if='field.slot'  :formData='formData' :name='field.value'></slot>
          <component
            v-else
            :is="field.type || 'Input'"
            v-bind="field.props"
            v-model="formData[field.value]"
            style="width: 100%"
          >
            <template v-if="field.type === 'Select'">
              <Option
                v-for="item in field.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </template>
          </component>
        </FormItem>
      </Col>
      <!-- 重点样式 margin-left / margin-right :auto -->
      <Col :span="span" style="marginLeft:auto;text-align:right ">
      <template v-if='hasSlot'>
        <slot name='action'></slot>
      </template>
      <template v-else>
          <Button type="primary" @click="submitForm">查询</Button>
          <Button class='ivu-ml-8' @click="resetForm">重置</Button>
      </template>
      </Col>
    </Row>
  </Form>
  </div>
</template>

<script lang='ts'>
    import cloneDeep from 'lodash/cloneDeep';

    export default {
        name: 'QueryForm',
        props: {
            fields: {
                type: Array,
                default: () => []
            },
            queryData: {
                type: Object,
                default: () => ({})
            },
            span: {
                type: Number,
                default: 6
            }

        },
        data () {
            return {
                formData: {
                    // page: 1,
                    // size: 10
                }

            }
        },
        watch: {
            formData () {
                this.$emit('update:queryData', this.formData)
            }
        },
        computed: {
            hasSlot () {
                return !!this.$scopedSlots.default
            }
        },
        methods: {
            submitForm () {
                this.formData.page = 1;
                this.formData.pagesize = 10;
                if (this.$parent && this.$parent.$refs.baseTable) {
                    this.$parent.$refs.baseTable.current = 1;
                }
                this.$emit('getData', this.formData)
            },
            resetForm () {
                this.$refs.ruleForm.resetFields()
                if (this.$parent && this.$parent.$refs.baseTable) {
                    this.$parent.$refs.baseTable.current = 1;
                }
                this.$emit('getData', this.formData)
            }
        },
        mounted () {
            this.formData = cloneDeep(this.queryData)
        }

    }
</script>

<style lang="less" scoped></style>
