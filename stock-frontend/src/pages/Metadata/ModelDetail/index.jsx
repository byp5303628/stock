import { PageContainer } from "@ant-design/pro-components";
import { Button, Card, Descriptions, message, Modal, Space, Table, Tag, Alert } from "antd";
import { useEffect, useState } from "react";
import { getModelDetail, validateModel } from "@/services/metadata";

const FIELD_TYPES_MAP = {
  'STRING': '字符串',
  'INTEGER': '整数',
  'DECIMAL': '小数',
  'DATE': '日期',
  'DATETIME': '日期时间',
  'BOOLEAN': '布尔',
};

const STATUS_MAP = {
  'DRAFT': { color: 'default', text: '草稿' },
  'PUBLISHED': { color: 'green', text: '已发布' },
  'DEPRECATED': { color: 'orange', text: '已废弃' },
};

const ModelDetail = () => {

  const [model, setModel] = useState(null);
  const [fields, setFields] = useState([]);
  const [validationResult, setValidationResult] = useState(null);
  const [validateVisible, setValidateVisible] = useState(false);

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
    });
  };

  const handleValidate = () => {
    validateModel({ modelId: Number(modelId) }).then(r => {
      if (r && r.code === 200) {
        setValidationResult(r.data);
        setValidateVisible(true);

        if (r.data.valid) {
          message.success("Schema 校验通过");
          // Refresh to get updated status (PUBLISHED)
          setTimeout(refreshModel, 500);
        } else {
          message.warning("Schema 校验未通过");
        }
      } else {
        message.error(r?.msg || "校验失败");
      }
    });
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
      render: (txt) => FIELD_TYPES_MAP[txt] || txt,
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
      render: (val) => val ? `#${val}` : '-',
    },
    {
      title: '排序', dataIndex: 'sortOrder', key: 'sortOrder2',
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
          <Button type="primary" onClick={handleValidate}>Schema 校验</Button>
          <Button onClick={() => {
            window.location.hash = `#/operation/metadata-model-list/model-editor?id=${model.id}`;
          }}>编辑字段</Button>
        </Space>
      }
      style={{ marginBottom: 24 }}
    >
      <Descriptions column={2}>
        <Descriptions.Item label="模型名称">{model.name}</Descriptions.Item>
        <Descriptions.Item label="模型编码">{model.code}</Descriptions.Item>
        <Descriptions.Item label="模型类型">{model.modelType}</Descriptions.Item>
        <Descriptions.Item label="描述">{model.description || '-'}</Descriptions.Item>
      </Descriptions>
    </Card>}

    <Card title="字段列表">
      <Table columns={columns} dataSource={fields} rowKey="id"/>
    </Card>

    <Modal
      title="Schema 校验结果"
      open={validateVisible}
      onCancel={() => setValidateVisible(false)}
      footer={<Button onClick={() => setValidateVisible(false)}>关闭</Button>}
      width={600}
    >
      {validationResult && (
        validationResult.valid ? (
          <Alert type="success" message="校验通过" description="所有字段配置符合 Schema 规范。" showIcon/>
        ) : (
          <div>
            <Alert type="warning" message="校验未通过" description={`发现 ${(validationResult.errors || []).length} 个问题。`} showIcon style={{ marginBottom: 16 }}/>
            <Table
              columns={[
                { title: '字段', dataIndex: 'field', key: 'field' },
                { title: '错误描述', dataIndex: 'message', key: 'message' },
              ]}
              dataSource={validationResult.errors || []}
              rowKey={(record, index) => index}
              pagination={false}
              size="small"
            />
          </div>
        )
      )}
    </Modal>
  </PageContainer>;
};

export default ModelDetail;
