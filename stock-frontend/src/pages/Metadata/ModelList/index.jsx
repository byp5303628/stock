import { PageContainer } from "@ant-design/pro-components";
import { Button, Card, Form, Input, message, Modal, Popconfirm, Select, Table, Tag, Space } from "antd";
import { useEffect, useState } from "react";
import { listModels, saveModel, deleteModel } from "@/services/metadata";
import { useForm } from "antd/es/form/Form";
import { formItemLayout } from "@/pages/common";

const MODEL_TYPES = [
  { value: 'TABLE', label: '表模型' },
  { value: 'INDICATOR', label: '指标模型' },
  { value: 'DIMENSION', label: '维度模型' },
];

const STATUS_MAP = {
  'DRAFT': { color: 'default', text: '草稿' },
  'PUBLISHED': { color: 'green', text: '已发布' },
  'DEPRECATED': { color: 'orange', text: '已废弃' },
};

const ModelList = () => {

  const [models, setModels] = useState([]);
  const [visible, setVisible] = useState(false);
  const [editingModel, setEditingModel] = useState(null);
  const [form] = useForm();

  useEffect(() => {
    refresh();
  }, []);

  const refresh = () => {
    listModels().then(r => {
      if (r && r.code === 200) {
        setModels(r.data || []);
      }
    }).catch(() => message.error('加载模型列表失败，请稍后重试'));
  };

  const columns = [
    {
      title: '模型编码', dataIndex: 'code', key: 'code',
    },
    {
      title: '模型名称', dataIndex: 'name', key: 'name',
    },
    {
      title: '模型类型', dataIndex: 'modelType', key: 'modelType',
      render: (txt) => {
        const item = MODEL_TYPES.find(t => t.value === txt);
        return item ? item.label : txt;
      }
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
      title: '描述', dataIndex: 'description', key: 'description',
    },
    {
      title: '更新时间', dataIndex: 'gmtModified', key: 'gmtModified',
    },
    {
      title: '操作', dataIndex: 'id', key: 'id',
      render: (id, record) => {
        return <Space>
          <a href={`#/operation/metadata-model-list/model-detail?id=${id}`}>查看详情</a>
          <a onClick={() => {
            setEditingModel(record);
            form.setFieldsValue(record);
            setVisible(true);
          }}>编辑</a>
          <Popconfirm
            title="确定要删除此模型吗？"
            description="删除后将同时删除关联的所有字段。"
            onConfirm={() => handleDelete(id)}
          >
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>;
      }
    },
  ];

  // H7: 先调用 validateFields 校验再提交
  const submit = () => {
    form.validateFields().then(() => {
      const payload = form.getFieldsValue();
      if (editingModel && editingModel.id) {
        payload.id = editingModel.id;
      }

      saveModel(payload).then(r => {
        if (r && r.code === 200) {
          message.success("保存成功");
          refresh();
          setVisible(false);
          setEditingModel(null);
          form.resetFields();
        } else {
          message.error(r?.msg || "保存失败");
        }
      }).catch(() => message.error('操作失败，请稍后重试'));
    });
  };

  const handleDelete = (id) => {
    deleteModel(id).then(r => {
      if (r && r.code === 200) {
        message.success("模型已删除");
        refresh();
      } else {
        message.error(r?.msg || "删除失败");
      }
    }).catch(() => message.error('操作失败，请稍后重试'));
  };

  const openCreateModal = () => {
    setEditingModel(null);
    form.resetFields();
    setVisible(true);
  };

  return <PageContainer title={"元数据模型管理"} content={"管理所有元数据模型的定义"}>
    <Card extra={
      <Button type={"primary"} onClick={openCreateModal}>新建模型</Button>
    }>
      <Table columns={columns} dataSource={models} rowKey="id"/>
    </Card>

    <Modal
      title={editingModel ? "编辑模型" : "新建模型"}
      open={visible}
      onOk={submit}
      onCancel={() => {
        setVisible(false);
        setEditingModel(null);
        form.resetFields();
      }}
    >
      <Form form={form}>
        <Form.Item {...formItemLayout} name={"code"} label={"模型编码"} rules={[{ required: true, message: '请输入模型编码' }]}>
          <Input disabled={!!editingModel}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"name"} label={"模型名称"} rules={[{ required: true, message: '请输入模型名称' }]}>
          <Input/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"modelType"} label={"模型类型"} rules={[{ required: true, message: '请选择模型类型' }]}>
          <Select options={MODEL_TYPES}/>
        </Form.Item>
        <Form.Item {...formItemLayout} name={"description"} label={"描述"}>
          <Input.TextArea rows={3}/>
        </Form.Item>
      </Form>
    </Modal>
  </PageContainer>;
};

export default ModelList;
