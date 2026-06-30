import { PageContainer } from "@ant-design/pro-components";
import {
  Button, Card, Descriptions, Drawer, Form, Input, InputNumber, message, Modal, Popconfirm,
  Select, Space, Switch, Table, Tag
} from "antd";
import { useEffect, useState } from "react";
import {
  getModelDetail, publishModel, getModelSchema, saveField, deleteField, listEnums,
  listModelVersions, switchModelVersion
} from "@/services/metadata";
import { useForm } from "antd/es/form/Form";
import { formItemLayout } from "@/pages/common";

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

const STATUS_MAP = {
  'DRAFT': { color: 'default', text: '草稿' },
  'PUBLISHED': { color: 'green', text: '已发布' },
  'DEPRECATED': { color: 'orange', text: '已废弃' },
  'CHANGING': { color: 'orange', text: '变更中' },
};

const ModelDetail = () => {

  const [model, setModel] = useState(null);
  const [fields, setFields] = useState([]);
  const [enums, setEnums] = useState([]);
  const [schemaJson, setSchemaJson] = useState(null);
  const [schemaVisible, setSchemaVisible] = useState(false);
  const [fieldVisible, setFieldVisible] = useState(false);
  const [editingField, setEditingField] = useState(null);
  const [versions, setVersions] = useState([]);
  const [versionsVisible, setVersionsVisible] = useState(false);
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

  const handleShowSchema = () => {
    getModelSchema(Number(modelId)).then(r => {
      if (r && r.code === 200) {
        setSchemaJson(r.data);
        setSchemaVisible(true);
      } else {
        message.error(r?.msg || "获取 JSON Schema 失败");
      }
    }).catch(() => message.error('获取 JSON Schema 失败，请稍后重试'));
  };

  const handlePublish = () => {
    publishModel({ modelId: Number(modelId) }).then(r => {
      if (r && r.code === 200) {
        message.success("发布成功");
        refreshModel();
      } else {
        message.error(r?.msg || "发布失败");
      }
    }).catch(() => message.error('发布失败，请稍后重试'));
  };

  const handleLoadVersions = () => {
    listModelVersions(Number(modelId)).then(r => {
      if (r && r.code === 200) {
        setVersions(r.data || []);
        setVersionsVisible(true);
      } else {
        message.error(r?.msg || "获取版本列表失败");
      }
    }).catch(() => message.error('获取版本列表失败，请稍后重试'));
  };

  const handleSwitchVersion = (version) => {
    Modal.confirm({
      title: '切换版本',
      content: `确定要切换到版本 v${version} 吗？`,
      onOk: () => {
        switchModelVersion({ modelId: Number(modelId), version }).then(r => {
          if (r && r.code === 200) {
            message.success(`已切换到版本 v${version}`);
            setVersionsVisible(false);
            refreshModel();
          } else {
            message.error(r?.msg || "切换版本失败");
          }
        }).catch(() => message.error('切换版本失败，请稍后重试'));
      }
    });
  };

  // ===== 字段管理 =====

  const openFieldModal = () => {
    setEditingField(null);
    form.resetFields();
    form.setFieldsValue({ required: false, modelId: Number(modelId) });
    setFieldVisible(true);
  };

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
          setFieldVisible(false);
          setEditingField(null);
          form.resetFields();
          refreshModel();
        } else {
          message.error(r?.msg || "字段保存失败");
        }
      }).catch(() => message.error('操作失败，请稍后重试'));
    });
  };

  const handleDeleteField = (fieldId) => {
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

  const fieldColumns = [
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
      render: (txt) => txt || '-',
    },
    {
      title: '必填', dataIndex: 'required', key: 'required',
      render: (val) => val ? <Tag color="red">是</Tag> : <Tag>否</Tag>
    },
    {
      title: '约束', dataIndex: 'constraints', key: 'constraints',
      render: (txt) => txt || '-',
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
            setFieldVisible(true);
          }}>编辑</a>
          <Popconfirm title="确定要删除此字段吗？" onConfirm={() => handleDeleteField(id)}>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>;
      }
    },
  ];

  const versionColumns = [
    { title: '版本号', dataIndex: 'version', key: 'version',
      render: (v) => `v${v}` },
    { title: '版本说明', dataIndex: 'versionDesc', key: 'versionDesc',
      render: (t) => t || '-' },
    { title: '发布时间', dataIndex: 'gmtCreate', key: 'gmtCreate' },
    { title: '当前版本', dataIndex: 'isCurrent', key: 'isCurrent',
      render: (val) => val ? <Tag color="green">当前</Tag> : null },
    { title: '操作', key: 'action',
      render: (_, record) => (
        <Button
          type="link"
          disabled={record.isCurrent}
          onClick={() => handleSwitchVersion(record.version)}
        >切换到此版本</Button>
      )
    },
  ];

  const renderStatus = (status) => {
    const item = STATUS_MAP[status];
    if (item) {
      return <Tag color={item.color}>{item.text}</Tag>;
    }
    return status;
  };

  return <PageContainer
    title={"模型详情"}
    onBack={() => window.history.back()}
  >
    {model && <Card
      title="模型基本信息"
      extra={
        <Space>
          {renderStatus(model.status)}
          <Button onClick={handleLoadVersions}>版本历史</Button>
          <Button onClick={handleShowSchema}>查看 JSON Schema</Button>
          <Button
            type="primary"
            onClick={handlePublish}
            disabled={model.status === 'PUBLISHED' || model.status === 'DEPRECATED'}
          >
            发布
          </Button>
        </Space>
      }
      style={{ marginBottom: 24 }}
    >
      <Descriptions column={2}>
        <Descriptions.Item label="模型编码">{model.code}</Descriptions.Item>
        <Descriptions.Item label="模型名称">{model.name}</Descriptions.Item>
        <Descriptions.Item label="模型类型">{model.modelType}</Descriptions.Item>
        <Descriptions.Item label="描述">{model.description || '-'}</Descriptions.Item>
        <Descriptions.Item label="当前版本">v{model.currentVersion > 0 ? model.currentVersion : '-'}</Descriptions.Item>
      </Descriptions>
    </Card>}

    <Card
      title="属性列表"
      extra={<Button type={"primary"} onClick={openFieldModal}>添加属性</Button>}
    >
      <Table columns={fieldColumns} dataSource={fields} rowKey="id"/>
    </Card>

    {/* JSON Schema 展示弹窗 */}
    <Modal
      title="JSON Schema"
      open={schemaVisible}
      onCancel={() => setSchemaVisible(false)}
      footer={
        <Space>
          <Button onClick={() => {
            navigator.clipboard.writeText(JSON.stringify(schemaJson, null, 2));
            message.success("已复制到剪贴板");
          }}>复制</Button>
          <Button onClick={() => setSchemaVisible(false)}>关闭</Button>
        </Space>
      }
      width={700}
    >
      {schemaJson && (
        <pre style={{
          background: '#1e1e1e',
          color: '#d4d4d4',
          padding: 16,
          borderRadius: 6,
          maxHeight: 500,
          overflow: 'auto',
          fontSize: 12,
          lineHeight: 1.6,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-all',
        }}>
          {JSON.stringify(schemaJson, null, 2)}
        </pre>
      )}
    </Modal>

    {/* 属性编辑弹窗 */}
    <Modal
      title={editingField ? "编辑属性" : "添加属性"}
      open={fieldVisible}
      onOk={submitField}
      onCancel={() => {
        setFieldVisible(false);
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

    {/* 版本历史抽屉 */}
    <Drawer
      title="版本历史"
      open={versionsVisible}
      onClose={() => setVersionsVisible(false)}
      width={500}
    >
      <Table
        columns={versionColumns}
        dataSource={versions}
        rowKey="id"
        pagination={false}
      />
    </Drawer>
  </PageContainer>;
};

export default ModelDetail;
