
import contansData from '../constans/index'

/**
 * @description 给数字补0
 * @param {Number} value
 * @returns {String}
 */
const addZero = function (value) {
    value = Math.round(parseFloat(value) * 100) / 100;
    var xsd = value.toString().split('.');
    if (xsd.length === 1) {
        value = value.toString() + '.00';
        return value;
    }
    if (xsd.length > 1) {
        if (xsd[1].length < 2) {
            value = value.toString() + '0';
        }
        return value;
    }
}
// 支付方式
const methodFilter = function (value) {
    let obj = contansData['PAYMENT_METHODS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}

// 支付渠道
const paymentChannelFilter = function (value) {
    let obj = contansData['PAYMENT_CHANNELS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 费用类型
const feeTypeFilter = function (value) {
    let obj = contansData['TYPE_LIST'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 费用项目
const projectFeeFilter = function (value) {
    let obj = contansData['ITEM_LIST'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 支付状态
const paymentStatusFilter = function (value) {
    let obj = contansData['PAYMENT_STATUS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 交易类型
const transactionStatusFilter = function (value) {
    let obj = contansData['TRANSACTION_STATUS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 推送状态
const pushStatusFilter = function (value) {
    let obj = contansData['PUSH_STATUS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}

// 入场类型 & 出场类型
const entryType = function (value) {
    let obj = contansData['ENTRY_TYPE'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
// 入场方式or 出场方式
const entryMethod = function (value) {
    let obj = contansData['ENTRY_METHODS'].filter(item => {
        return item.value === Number(value)
    })[0]
    return obj ? obj.label : ''
}
export default {
    addZero,
    methodFilter,
    paymentChannelFilter,
    feeTypeFilter,
    projectFeeFilter,
    paymentStatusFilter,
    transactionStatusFilter,
    pushStatusFilter,
    entryType,
    entryMethod
}
