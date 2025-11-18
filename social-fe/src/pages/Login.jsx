import { useState } from 'react'
import { Button, Card, Form, Input, Tabs, message, Space, Typography } from 'antd'
import { login, register } from '../api/client.js'

const { Title, Text } = Typography

export default function Login({ onLoginSuccess }) {
  const [loginForm] = Form.useForm()
  const [registerForm] = Form.useForm()
  const [loginLoading, setLoginLoading] = useState(false)
  const [registerLoading, setRegisterLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('login')

  async function handleLogin(values) {
    setLoginLoading(true)
    try {
      const response = await login(values.username, values.password)
      onLoginSuccess(response)
      message.success('Đăng nhập thành công!')
    } catch (e) {
      message.error(e.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.')
    } finally {
      setLoginLoading(false)
    }
  }

  async function handleRegister(values) {
    if (values.password !== values.confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp')
      return
    }
    setRegisterLoading(true)
    try {
      await register({
        email: values.email,
        username: values.username,
        password: values.password,
        avatarUrl: values.avatarUrl || null,
        bio: values.bio || null
      })
      message.success('Đăng ký thành công! Vui lòng đăng nhập.')
      setActiveTab('login')
      loginForm.setFieldsValue({ username: values.email })
      registerForm.resetFields()
    } catch (e) {
      message.error(e.message || 'Đăng ký thất bại. Email có thể đã được sử dụng.')
    } finally {
      setRegisterLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      padding: 20
    }}>
      <Card
        style={{
          width: '100%',
          maxWidth: 450,
          boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
          borderRadius: 16
        }}
      >
        <Space direction="vertical" size="large" style={{ width: '100%', textAlign: 'center' }}>
          <div>
            <Title level={2} style={{ margin: 0, color: '#1677ff' }}>Mini Social</Title>
            <Text type="secondary">Kết nối và chia sẻ với bạn bè</Text>
          </div>

          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={[
              {
                key: 'login',
                label: 'Đăng nhập',
                children: (
                  <Form
                    form={loginForm}
                    layout="vertical"
                    onFinish={handleLogin}
                    size="large"
                  >
                    <Form.Item
                      name="username"
                      label="Email"
                      rules={[
                        { required: true, message: 'Vui lòng nhập email' },
                        { type: 'email', message: 'Email không hợp lệ' }
                      ]}
                    >
                      <Input placeholder="your@email.com" />
                    </Form.Item>
                    <Form.Item
                      name="password"
                      label="Mật khẩu"
                      rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
                    >
                      <Input.Password placeholder="Nhập mật khẩu" />
                    </Form.Item>
                    <Form.Item>
                      <Button type="primary" htmlType="submit" block loading={loginLoading}>
                        Đăng nhập
                      </Button>
                    </Form.Item>
                  </Form>
                )
              },
              {
                key: 'register',
                label: 'Đăng ký',
                children: (
                  <Form
                    form={registerForm}
                    layout="vertical"
                    onFinish={handleRegister}
                    size="large"
                  >
                    <Form.Item
                      name="email"
                      label="Email"
                      rules={[
                        { required: true, message: 'Vui lòng nhập email' },
                        { type: 'email', message: 'Email không hợp lệ' }
                      ]}
                    >
                      <Input placeholder="your@email.com" />
                    </Form.Item>
                    <Form.Item
                      name="username"
                      label="Tên người dùng"
                      rules={[{ required: true, message: 'Vui lòng nhập tên người dùng' }]}
                    >
                      <Input placeholder="Tên hiển thị" />
                    </Form.Item>
                    <Form.Item
                      name="password"
                      label="Mật khẩu"
                      rules={[
                        { required: true, message: 'Vui lòng nhập mật khẩu' },
                        { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' }
                      ]}
                    >
                      <Input.Password placeholder="Tối thiểu 6 ký tự" />
                    </Form.Item>
                    <Form.Item
                      name="confirmPassword"
                      label="Xác nhận mật khẩu"
                      rules={[
                        { required: true, message: 'Vui lòng xác nhận mật khẩu' },
                        ({ getFieldValue }) => ({
                          validator(_, value) {
                            if (!value || getFieldValue('password') === value) {
                              return Promise.resolve()
                            }
                            return Promise.reject(new Error('Mật khẩu xác nhận không khớp'))
                          }
                        })
                      ]}
                    >
                      <Input.Password placeholder="Nhập lại mật khẩu" />
                    </Form.Item>
                    <Form.Item
                      name="avatarUrl"
                      label="Avatar URL (tùy chọn)"
                    >
                      <Input placeholder="https://example.com/avatar.jpg" />
                    </Form.Item>
                    <Form.Item
                      name="bio"
                      label="Giới thiệu (tùy chọn)"
                    >
                      <Input.TextArea rows={2} placeholder="Giới thiệu về bản thân..." />
                    </Form.Item>
                    <Form.Item>
                      <Button type="primary" htmlType="submit" block loading={registerLoading}>
                        Đăng ký
                      </Button>
                    </Form.Item>
                  </Form>
                )
              }
            ]}
          />
        </Space>
      </Card>
    </div>
  )
}

