import {QuestionCircleOutlined} from '@ant-design/icons';
import {SelectLang as UmiSelectLang} from '@umijs/max';
import {DownOutlined, SmileOutlined} from '@ant-design/icons';
import {Dropdown, Space, Select, Modal, Card, Form, message} from 'antd';
import React, {useEffect, useState} from 'react';
import {useForm} from "antd/es/form/Form";
import {history} from '@umijs/max';

export const SelectLang = () => {
    return (
        <UmiSelectLang
            style={{
                padding: 4,
            }}
        />
    );
};
export const Question = () => {
    const isOnline = process.env.DEPLOY_ENV === 'production';
    const [visible, setVisible] = useState(false);
    const [bizLines, setBizLines] = useState([]);
    const [permissions, setPermissions] = useState({});
    const [selectBizLine, setSelectBizLine] = useState(0);
    const [form] = useForm();

    let items;
    if (isOnline) {
        items = [
            {
                key: '2',
                label: (
                    <a target="_blank" rel="noopener noreferrer" href="https://metadata.nibmp.test.sankuai.com">
                        线下
                    </a>
                )
            },
        ];
    } else {
        items = [
            {
                key: '1',
                label: (
                    <a target="_blank" rel="noopener noreferrer" href="https://mpmetadata.sankuai.com">
                        线上
                    </a>
                )
            }
        ];
    }

    const filterOption = (input, option) =>
        (option?.label ?? '').toLowerCase().includes(input.toLowerCase());

    return (
        <>
            <div
                style={{
                    fontSize: '14px'
                }}
                onClick={() => {
                    window.open('https://km.sankuai.com/collabpage/1293478200');
                }}
            >
                <QuestionCircleOutlined/>
                <span> 用户指南</span>
            </div>
        </>
    );
};
