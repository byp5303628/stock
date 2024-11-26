import { Form, Input, Radio, Select } from "antd";
import { formItemLayout } from "@/pages/common";

const FormItem = Form.Item

export const metaDataInputForm = (form, fields) => {
  const forms = [];

  fields.forEach(field => {

    let input = undefined;

    if (field.fieldType === 200) {
      input = (
        <Radio.Group options={['true', 'false', 'null']} defaultValue={'null'}>
        </Radio.Group>
      );
    } else if (field.verificationJsonSchema) {

      const enumsConfig = JSON.parse(field.verificationJsonSchema).enum;
      console.log(enumsConfig);

      if (enumsConfig) {
        const options = [];

        enumsConfig.forEach(o => {
          options.push(
            <Select.Option value={o}>{o}</Select.Option>
          );
        })
        input = (
          <Select placeholder={"请选择对应的" + field.fieldCName}>
            {options}
          </Select>
        )
      } else {
        input = (<Input
          placeholder={"请输入" + field.fieldCName}
          style={{ minHeight: 20 }}
          rows={4}
        />);
      }
    } else {
      input = (<Input
        placeholder={"请输入" + field.fieldCName}
        style={{ minHeight: 20 }}
        rows={4}
      />);
    }

    forms.push(
      <FormItem {...formItemLayout} label={field.fieldName} name={field.fieldName}>
        {input}
      </FormItem>
    )
  })

  return (
    <Form form={form}>
      {forms}
    </Form>
  );
}

