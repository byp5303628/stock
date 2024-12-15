import { PageContainer } from "@ant-design/pro-components";
import { Button, Card, Form, Input, message, Modal, Table, Tag } from "antd";
import { useEffect, useState } from "react";
import { getAllScheduleConfigs, saveScheduleConfig } from "@/services/schedule-config";
import { useForm } from "antd/es/form/Form";
import { formItemLayout } from "@/pages/common";

const ScheduleConfigList = () => {

    const [scheduleConfigs, setScheduleConfigs] = useState([]);
    const [visible, setVisible] = useState(false);
    const [form] = useForm();

    useEffect(() => {
        refresh();
    }, []);

    const refresh = () => {
        getAllScheduleConfigs().then(r => {
            if (r && r.code === 200) {
                setScheduleConfigs(r.data);
            }
        })
    }

    const columns = [{
        title: 'ID', dataIndex: 'id', key: 'id',
    }, {
        title: '任务类型', dataIndex: 'taskType', key: 'taskType',
    }, {
        title: '调度配置', dataIndex: 'cronExpression', key: 'cronExpression',
    }, {
        title: '消费数量', dataIndex: 'count', key: 'count',
    }, {
        title: '状态', dataIndex: 'status', key: 'status', render: (txt) => {
            if (txt === "T") {
                return <Tag color={"green"}>生效</Tag>
            } else {
                return <Tag color={"red"}>失效</Tag>
            }
        }
    }, {
        title: '创建时间', dataIndex: 'gmtCreate', key: 'gmtCreate',
    }, {
        title: '更新时间', dataIndex: 'gmtModified', key: 'gmtModified',
    }, {
        title: '操作', dataIndex: 'id', key: 'id', render: (id) => {
            return <div>
                <a href={"#"} onClick={() => {
                    setVisible(true);
                }}>修改</a>
            </div>
        }
    }];

    const submit = () => {
        const payload = form.getFieldsValue();

        saveScheduleConfig(payload).then(r => {
            if (r && r.code === 200) {
                message.success("保存成功");
            }
        });

        setTimeout(refresh, 1000);

        setVisible(false);
    }

    return <PageContainer title={"调度配置管理"} content={"用于管理全部调度配置"}>
        <Card extra={
            <Button type={"primary"}
                    onClick={() => setVisible(true)}>
                新增
            </Button>}>
            <Table columns={columns} dataSource={scheduleConfigs}/>
        </Card>

        <Modal
            title={"调整任务配置"}
            open={visible}
            onOk={submit}
            onCancel={() => {
                setVisible(false)
            }}
        >
            <Form form={form}>
                <Form.Item {...formItemLayout} name={"taskType"} label={"任务类型"}>
                    <Input/>
                </Form.Item>
                <Form.Item {...formItemLayout} name={"cronExpression"} label={"调度配置"}>
                    <Input/>
                </Form.Item>
                <Form.Item {...formItemLayout} name={"count"} label={"消费数量"}>
                    <Input/>
                </Form.Item>
                <Form.Item {...formItemLayout} name={"status"} label={"状态"}>
                    <Input/>
                </Form.Item>
            </Form>
        </Modal>
    </PageContainer>
}

export default ScheduleConfigList;