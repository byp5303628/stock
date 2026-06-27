import { PageContainer } from "@ant-design/pro-components";
import {
  Button, Card, Descriptions, Form, Input, InputNumber, message, Modal, Popconfirm,
  Select, Space, Table, Tag
} from "antd";
import { MinusCircleOutlined, PlusOutlined } from "@ant-design/icons";
import { useEffect, useState } from "react";
import { listEnums, getEnumDetail, saveEnum, deleteEnum, getEnumUsage } from "@/services/metadata";
import { useForm } from "antd/es/form/Form";
import { formItemLayout } from "@/pages/common";

const STATUS_MAP = {
  'ACTIVE': { color: 'green', text: '启用' },
  'DELETED': { color: 'red', text: '已删除' },
};

const EnumManager = () => {

  const [enums, setEnums] = useState([]);
  const [visible, setVisible] = useState(false);
  const [usageVisible, setUsageVisible] = useState(false);
  const [editingEnum, setEditingEnum] = useState(null);
  const [usageData, setUsageData] = useState(null);
  const [form] = useForm();

  useEffect(() => {
    refresh();
  }, []);

  const refresh = () => {
    listEnums().then(r => {
      if (r && r.code === 200) {
        setEnums(r.data || []);
      }
    }).catch(() => message.error('加载枚举列表失败，请稍后重试'));
  };

  const columns = [
    {
      title: '枚举名称', dataIndex: 'name', key: 'name',
    },
    {
      title: '枚举编码', dataIndex: 'code', key: 'code',
    },
    {
      title: '描述', dataIndex: 'description', key: 'description',
      render: (txt) => txt || '-',
    },
    {
      title: '状态', dataIndex: 'status', key: 'status',
      render: (txt) => {
        const item = STATUS_MAP[txt];
        if (item) {
          return <Tag color={item.color}>{item.text}</Tag>;
        }
        return txt;
      }
    },
    {
      title: '引用模型数', dataIndex: 'refModelCount', key: 'refModelCount',
      render: (count, record) => {
        if (count > 0) {
          return <a onClick={() => showUsage(record)}>{count}</a>;
        }
        return count;
      }
    },
    {
      title: '引用字段数', dataIndex: 'refFieldCount', key: 'refFieldCount',
      render: (count, record) => {
        if (count > 0) {
          return <a onClick={() => showUsage(record)}>{count}</a>;
        }
        return count;
      }
    },
    {
      title: '操作', dataIndex: 'id', key: 'id',
      render: (id, record) => {
        return <Space>
          <a onClick={() => {
            // Load full detail with values
            getEnumDetail(id).then(r => {
              if (r && r.code === 200) {
                const detail = r.data;
                setEditingEnum(detail);
                form.setFieldsValue({
                  name: detail.name,
                  code: detail.code,
                  description: detail.description,
                  values: (detail.values || []).map(v => ({
                    valueCode: v.valueCode,
                    valueLabel: v.valueLabel,
                    sortOrder: v.sortOrder,
                  })),
                });
                setVisible(true);
              }
            }).catch(() => message.error('加载枚举详情失败，请稍后重试'));
          }}>编辑</a>
          <Popconfirm
            title={record.refModelCount > 0 || record.refFieldCount > 0
              ? `该枚举被 ${record.refModelCount} 个模型、${record.refFieldCount} 个字段引用，无法删除。`
              : "确定要删除此枚举吗？"}
            disabled={record.refModelCount > 0 || record.refFieldCount > 0}
            onConfirm={() => handleDelete(id)}
            okButtonProps={{ danger: record.refModelCount === 0 && record.refFieldCount === 0 }}
          >
            <a style={{ color: (record.refModelCount > 0 || record.refFieldCount > 0) ? '#ccc' : undefined }}>
              删除
            </a>
          </Popconfirm>
        </Space>;
      }
    },
  ];

  const openCreateModal = () => {
    setEditingEnum(null);
    form.resetFields();
    form.setFieldsValue({ values: [{ valueCode: '', valueLabel: '', sortOrder: 0 }] });
    setVisible(true);
  };

  const showUsage = (record) => {
    getEnumUsage(record.id).then(r => {
      if (r && r.code === 200) {
        setUsageData(r.data);
        setUsageVisible(true);
      }
    }).catch(() => message.error('加载引用信息失败，请稍后重试'));
  };

  // H7: 先调用 validateFields 再提交
  const submitEnum = () => {
    form.validateFields().then(() => {
      const payload = form.getFieldsValue();

      if (editingEnum && editingEnum.id) {
        payload.id = editingEnum.id;
      }

      saveEnum(payload).then(r => {
        if (r && r.code === 200) {
          message.success("保存成功");
          setVisible(false);
          setEditingEnum(null);
          form.resetFields();
          refresh();
        } else {
          message.error(r?.msg || "保存失败");
        }
      }).catch(() => message.error('操作失败，请稍后重试'));
    });
  };

  // C5: 使用真正的删除接口 deleteEnum，而非 saveEnum 伪装删除
  const handleDelete = (id) => {
    deleteEnum(id).then(r => {
      if (r && r.code === 200) {
        message.success("枚举已删除");
        refresh();
      } else {
        message.error(r?.msg || "删除失败");
      }
    }).catch(() => message.error('操作失败，请稍后重试'));
  };

  const usageColumns = [
    { title: '模型名称', dataIndex: 'modelName', key: 'modelName' },
    {
      title: '引用字段', dataIndex: 'fields', key: 'fields',
      render: (fields) => {
        if (!fields || fields.length === 0) return '-';
        return fields.map(f => f.fieldName).join('、');
      }
    },
  ];

  return <PageContainer title={"枚举管理"} content={"管理所有枚举定义及枚举值"}>
    <Card extra={
      <Button type={"primary"} onClick={openCreateModal}>新建枚举</Button>
    }>
      <Table columns={columns} dataSource={enums} rowKey="id"/>
    </Card>

    <Modal
      title={editingEnum ? "编辑枚举" : "新建枚举"}
      open={visible}
      onOk={submitEnum}
      onCancel={() => {
        setVisible(false);
        setEditingEnum(null);
        form.resetFields();
      }}
      width={700}
    >
      <Form form={form}>
        <Form.Item {...formItemLayout} name={"name"} label={"枚举名称"} rules={[{ required: true, message: '请输入枚举名称' }]}>
          <Input/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"code"} label={"枚举编码"} rules={[{ required: true, message: '请输入枚举编码' }]}>
          <Input disabled={!!editingEnum}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"description"} label={"描述"}>
          <Input.TextArea rows={2}/>
        </Form.Item>

        <Form.Item label={"枚举值列表"}>
          <Form.List name={"values"}>
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                    <Form.Item
                      {...restField}
                      name={[name, 'valueCode']}
                      rules={[{ required: true, message: '请输入值编码' }]}
                    >
                      <Input placeholder="值编码"/>
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, 'valueLabel']}
                      rules={[{ required: true, message: '请输入值标签' }]}
                    >
                      <Input placeholder="值标签"/>
                    </Form.Item>
                    <Form.Item
                      {...restField}
                      name={[name, 'sortOrder']}
                    >
                      <InputNumber placeholder="排序" min={0} style={{ width: 80 }}/>
                    </Form.Item>
                    <MinusCircleOutlined onClick={() => remove(name)}/>
                  </Space>
                ))}
                <Form.Item>
                  <Button type="dashed" onClick={() => add({ valueCode: '', valueLabel: '', sortOrder: 0 })} block icon={<PlusOutlined/>}>
                    新增枚举值
                  </Button>
                </Form.Item>
              </>
            )}
          </Form.List>
        </Form.Item>
      </Form>
    </Modal>

    <Modal
      title="枚举引用详情"
      open={usageVisible}
      onCancel={() => setUsageVisible(false)}
      footer={<Button onClick={() => setUsageVisible(false)}>关闭</Button>}
      width={600}
    >
      {usageData && <div>
        <Descriptions column={2} style={{ marginBottom: 16 }}>
          <Descriptions.Item label="枚举名称">{usageData.enumName}</Descriptions.Item>
          <Descriptions.Item label="总引用数">{usageData.totalRefCount}</Descriptions.Item>
          <Descriptions.Item label="引用模型数">{usageData.refModelCount}</Descriptions.Item>
          <Descriptions.Item label="引用字段数">{usageData.refFieldCount}</Descriptions.Item>
        </Descriptions>
        <Table
          columns={usageColumns}
          dataSource={usageData.refDetails || []}
          rowKey="modelId"
          pagination={false}
          size="small"
        />
      </div>}
    </Modal>
  </PageContainer>;
};

export default EnumManager;
