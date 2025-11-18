import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  Avatar,
  ConfigProvider,
  Dropdown,
  Layout,
  Menu,
  Space,
  Spin,
  Typography,
  message
} from 'antd'
import { LogoutOutlined, UserOutlined } from '@ant-design/icons'
import Friends from './pages/Friends.jsx'
import Feed from './pages/Feed.jsx'
import Messages from './pages/Messages.jsx'
import Login from './pages/Login.jsx'
import { setAuthToken, clearAuthToken, fetchUsers, fetchCurrentUser } from './api/client.js'

const menuItems = [
  { key: 'feed', label: 'Bảng tin' },
  { key: 'messages', label: 'Tin nhắn' },
  { key: 'friends', label: 'Bạn bè' }
]

function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [users, setUsers] = useState([])
  const [token, setToken] = useState(() => localStorage.getItem('authToken'))
  const [loading, setLoading] = useState(false)
  const [activeKey, setActiveKey] = useState('feed')

  useEffect(() => {
    let cancelled = false

    async function bootstrap() {
      if (!token) {
        clearAuthToken()
        setCurrentUser(null)
        setUsers([])
        return
      }

      setAuthToken(token)
      setLoading(true)
      try {
        const [profile, directory] = await Promise.all([
          fetchCurrentUser(),
          fetchUsers()
        ])
        if (cancelled) {
          return
        }
        setCurrentUser(profile || null)
        setUsers(directory || [])
      } catch (e) {
        if (cancelled) {
          return
        }
        console.error('Failed to bootstrap user session:', e)
        message.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
        handleLogout(true)
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    bootstrap()

    return () => {
      cancelled = true
    }
  }, [token])

  async function handleLoginSuccess(loginResponse) {
    const accessToken = loginResponse?.accessToken
    if (!accessToken) {
      message.error('Không nhận được token đăng nhập hợp lệ.')
      return
    }
    setAuthToken(accessToken)
    localStorage.setItem('authToken', accessToken)
    setToken(accessToken)
  }

  function handleLogout(silent = false) {
    clearAuthToken()
    setToken(null)
    setCurrentUser(null)
    setUsers([])
    localStorage.removeItem('authToken')
    if (!silent) {
      message.info('Đã đăng xuất')
    }
  }

  const content = useMemo(() => {
    if (!token) {
      return <Login onLoginSuccess={handleLoginSuccess} />
    }

    if (loading || !currentUser) {
      return (
        <div style={{ padding: 64, textAlign: 'center' }}>
          <Spin size="large" />
        </div>
      )
    }

    switch (activeKey) {
      case 'messages':
        return <Messages currentUser={currentUser} users={users} />
      case 'friends':
        return <Friends currentUser={currentUser} knownUsers={users} />
      case 'feed':
      default:
        return <Feed currentUser={currentUser} />
    }
  }, [token, currentUser, activeKey, users, loading])

  const userMenuItems = [
    {
      key: 'logout',
      label: 'Đăng xuất',
      icon: <LogoutOutlined />,
      onClick: handleLogout
    }
  ]

  if (!token || !currentUser) {
    return (
      <ConfigProvider theme={{ token: { colorPrimary: '#1677ff' } }}>
        {content}
      </ConfigProvider>
    )
  }

  return (
    <ConfigProvider theme={{ token: { colorPrimary: '#1677ff' } }}>
      <Layout style={{ minHeight: '100vh' }}>
        <Layout.Sider breakpoint="lg" collapsedWidth="0" width={200}>
          <div style={{ 
            color: '#fff', 
            padding: 16, 
            fontSize: 20, 
            fontWeight: 700,
            textAlign: 'center',
            borderBottom: '1px solid rgba(255,255,255,0.1)'
          }}>
            Mini Social
          </div>
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[activeKey]}
            items={menuItems}
            onClick={({ key }) => setActiveKey(key)}
            style={{ marginTop: 16 }}
          />
        </Layout.Sider>
        <Layout>
          <Layout.Header style={{ 
            background: '#fff', 
            padding: '0 24px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <Typography.Title level={4} style={{ margin: 0 }}>
              {menuItems.find(item => item.key === activeKey)?.label}
            </Typography.Title>
            <Space align="center">
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space 
                  style={{ 
                    cursor: 'pointer',
                    padding: '4px 12px',
                    borderRadius: 8,
                    transition: 'background 0.2s'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#f5f5f5'}
                  onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                >
                  <Avatar src={currentUser.avatarUrl} icon={<UserOutlined />}>
                    {currentUser.username?.[0]?.toUpperCase()}
                  </Avatar>
                  <div style={{ lineHeight: 1.4 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{currentUser.username}</div>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {currentUser.email}
                    </Typography.Text>
                  </div>
                </Space>
              </Dropdown>
            </Space>
          </Layout.Header>
          <Layout.Content style={{ padding: 24, background: '#f0f2f5' }}>
            {content}
          </Layout.Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  )
}

export default App
