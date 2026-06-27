import { PageContainer } from "@ant-design/pro-components";
import {
  Button, Card, Descriptions, Form, Input, InputNumber, message, Modal, Popconfirm,
  Select, Space, Switch, Table, Tag
} from "antd";
import { useEffect, useState } from "react";
import { getModelDetail, saveField, deleteField, listEnums } from "@/services/metadata";
import { useForm } from "antd/es/form/Form";
import { formItemLayout } from "@/pages/common";

// C6: 前端 FIELD_TYPES 扩展，与后端 VALID_FIELD_TYPES 保持一致，并加入 ENUM 选项
const FIELD_TYPES = [
  { value: 'STRING', label: '字符串' },
  { value: 'NUMBER', label: '数字' },
  { value: 'INTEGER', label: '整数' },
  { value: 'DECIMAL', label: '小数' },
  { value: 'DATE', label: '日期' },
  { value: 'DATETIME', label: '日期时间' },
  { value: 'BOOLEAN', label: '布尔' },
  { value: 'ENUM', label: '枚举' },
];

const ModelEditor = () => {

  const [model, setModel] = useState(null);
  const [fields, setFields] = useState([]);
  const [enums, setEnums] = useState([]);
  const [visible, setVisible] = useState(false);
  const [editingField, setEditingField] = useState(null);
  const [form] = useForm();

  const modelId = new URLSearchParams(window.location.hash.split('?')[1] || '').get('id');

  useEffect(() => {
    if (modelId) {
      refreshModel();
    }
  }, [modelId]);

  const refreshModel = () => {
    if (!modelId) return;

    getModelDetail(Number(modelId)).then(r => {
      if (r && r.code === 200) {
        setModel(r.data);
        setFields(r.data.fields || []);
      }
    }).catch(() => message.error('加载模型详情失败，请稍后重试'));

    listEnums().then(r => {
      if (r && r.code === 200) {
        setEnums(r.data || []);
      }
    }).catch(() => message.error('加载枚举列表失败，请稍后重试'));
  };

  const columns = [
    {
      title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 60,
    },
    {
      title: '字段名称', dataIndex: 'fieldName', key: 'fieldName',
    },
    {
      title: '字段类型', dataIndex: 'fieldType', key: 'fieldType',
      render: (txt) => {
        const item = FIELD_TYPES.find(t => t.value === txt);
        return item ? item.label : txt;
      }
    },
    {
      title: '业务含义', dataIndex: 'businessMeaning', key: 'businessMeaning',
    },
    {
      title: '必填', dataIndex: 'required', key: 'required',
      render: (val) => val ? <Tag color="red">是</Tag> : <Tag>否</Tag>
    },
    {
      title: '绑定枚举', dataIndex: 'enumId', key: 'enumId',
      render: (enumId) => {
        if (!enumId) return '-';
        const e = enums.find(item => item.id === enumId);
        return e ? e.name : `#${enumId}`;
      }
    },
    {
      title: '操作', dataIndex: 'id', key: 'id',
      render: (id, record) => {
        return <Space>
          <a onClick={() => {
            setEditingField(record);
            form.setFieldsValue({
              ...record,
              required: record.required || false,
            });
            setVisible(true);
          }}>编辑</a>
          <Popconfirm title="确定要删除此字段吗？" onConfirm={() => handleDelete(id)}>
            <a>删除</a>
          </Popconfirm>
        </Space>;
      }
    },
  ];

  const openCreateModal = () => {
    setEditingField(null);
    form.resetFields();
    form.setFieldsValue({ required: false, modelId: Number(modelId) });
    setVisible(true);
  };

  // H7: 先调用 validateFields 校验再提交
  const submitField = () => {
    form.validateFields().then(() => {
      const payload = form.getFieldsValue();
      payload.modelId = Number(modelId);

      if (editingField && editingField.id) {
        payload.id = editingField.id;
      }

      saveField(payload).then(r => {
        if (r && r.code === 200) {
          message.success("字段保存成功");
          setVisible(false);
          setEditingField(null);
          form.resetFields();
          refreshModel();
        } else {
          message.error(r?.msg || "字段保存失败");
        }
      }).catch(() => message.error('操作失败，请稍后重试'));
    });
  };

  const handleDelete = (fieldId) => {
    deleteField(fieldId).then(r => {
      if (r && r.code === 200) {
        message.success("字段已删除");
        refreshModel();
      } else {
        message.error(r?.msg || "删除失败");
      }
    }).catch(() => message.error('操作失败，请稍后重试'));
  };

  const getEnumUsageInfo = (enumId) => {
    if (!enumId) return '';
    const e = enums.find(item => item.id === enumId);
    if (!e) return '';
    const parts = [];
    if (e.refModelCount > 0) parts.push(`${e.refModelCount} 个模型使用`);
    if (e.refFieldCount > 0) parts.push(`${e.refFieldCount} 个字段引用`);
    return parts.length > 0 ? parts.join('，') : '暂无引用';
  };

  return <PageContainer
    title={"模型编辑"}
    onBack={() => window.history.back()}
  >
    {model && <Card title="模型基本信息" style={{ marginBottom: 24 }}>
      <Descriptions column={2}>
        <Descriptions.Item label="模型名称">{model.name}</Descriptions.Item>
        <Descriptions.Item label="模型编码">{model.code}</Descriptions.Item>
        <Descriptions.Item label="模型类型">{model.modelType}</Descriptions.Item>
        <Descriptions.Item label="状态">{model.status}</Descriptions.Item>
        <Descriptions.Item label="描述" span={2}>{model.description || '-'}</Descriptions.Item>
      </Descriptions>
    </Card>}

    <Card
      title="字段列表"
      extra={<Button type={"primary"} onClick={openCreateModal}>新增字段</Button>}
    >
      <Table columns={columns} dataSource={fields} rowKey="id"/>
    </Card>

    <Modal
      title={editingField ? "编辑字段" : "新增字段"}
      open={visible}
      onOk={submitField}
      onCancel={() => {
        setVisible(false);
        setEditingField(null);
        form.resetFields();
      }}
      width={600}
    >
      <Form form={form}>
        <Form.Item {...formItemLayout} name={"fieldName"} label={"字段名称"} rules={[{ required: true, message: '请输入字段名称' }]}>
          <Input/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"fieldType"} label={"字段类型"} rules={[{ required: true, message: '请选择字段类型' }]}>
          <Select options={FIELD_TYPES}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"businessMeaning"} label={"业务含义"}>
          <Input.TextArea rows={2}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"required"} label={"是否必填"} valuePropName="checked">
          <Switch/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"sortOrder"} label={"排序序号"}>
          <InputNumber min={0} style={{ width: '100%' }}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"enumId"} label={"绑定枚举"}>
          <Select
            allowClear
            placeholder="选择枚举（可选）"
            options={enums.map(e => ({
              value: e.id,
              label: `${e.name} (${e.code})`,
              extra: getEnumUsageInfo(e.id),
            }))}
            optionRender={(option) => (
              <div>
                <div>{option.label}</div>
                {option.data.extra && <div style={{ fontSize: 12, color: '#999' }}>{option.data.extra}</div>}
              </div>
            )}
          />
        </Form.Item>
      </Form>
    </Modal>
  </PageContainer>;
};

export default ModelEditor;
