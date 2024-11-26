import { message, Tag } from 'antd';
import JSONBig from "json-bigint";
import React from "react";

export const formItemLayout = {
    labelCol: {
        xs: { span: 24 },
        sm: { span: 7 }
    },
    wrapperCol: {
        xs: { span: 24 },
        sm: { span: 12 },
        md: { span: 10 }
    }
}

export function isValidJSON(jsonString) {
    try {
        JSON.parse(jsonString);
        return true;
    } catch (e) {
        return false;
    }
}

export function formatJson(jsonString, warning = true) {
    if (!isValidJSON(jsonString)) {
        if (warning) {
            message.warning('需要格式化的信息不是合法的json!');
        }
        return jsonString;
    }

    const json = JSONBig.parse(jsonString);

    return JSON.stringify(json, null, '\t');
}

export function formatTimestamp(timestamp) {
    if (!timestamp) {
        return undefined;
    }

    let date = new Date(timestamp);
    let year = date.getFullYear(); // 获取年份
    let month = date.getMonth() + 1; // 获取月份，注意月份是从0开始的，所以需要+1
    let day = date.getDate(); // 获取日期
    let hour = date.getHours(); // 获取小时
    let minute = date.getMinutes(); // 获取分钟
    let second = date.getSeconds(); // 获取秒

// 按照 yyyy-mm-dd hh:mm:ss 的格式拼接字符串
    let formattedTime = `${year}-${month < 10 ? '0' + month : month}-${day < 10 ? '0' + day : day} ${hour < 10 ? '0' + hour : hour}:${minute < 10 ? '0' + minute : minute}:${second < 10 ? '0' + second : second}`;

    return formattedTime;
}

export function getTimestamp(dateStr) {
    return new Date(dateStr).getTime()
}
export function renderEntityLink(entityName, bizLine = 23) {
    if (!entityName) {
        return undefined;
    }
    return <a href={`#/metadata-model/model-detail?entityName=${entityName}&bizLine=${bizLine}`} target="_blank">{entityName}</a>
}

export function renderSupplyStorage(record) {
    if (!record) {
        return null;
    }

    if (record.kvStorage) {
        return <Tag color={"blue"}>{record.kvStorage}</Tag>;
    } else if (record.realtime === 'true') {
        return <Tag color={"red"}>RDS</Tag>;
    } else if (record.realtime === 'false') {
        return <Tag color={"yellow"}>BLADE</Tag>;
    } else {
        return null;
    }
}

export function getSupplyStorage(record) {
    if (!record) {
        return null;
    }

    if (record.kvStorage) {
        return 'KV';
    } else if (record.realtime === 'true') {
        return 'RDS';
    } else if (record.realtime === 'false') {
        return 'BLADE'
    } else {
        return null;
    }
}

