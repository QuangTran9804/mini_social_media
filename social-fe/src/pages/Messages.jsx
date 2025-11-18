import { useEffect, useMemo, useRef, useState } from 'react'
import { Avatar, Button, Card, Empty, Input, List, Space, Typography, message } from 'antd'
import { fetchMessagesWith, markMessageRead, sendMessageTo } from '../api/client.js'

const { TextArea } = Input

export default function Messages({ currentUser, users }) {
  const [selectedUserId, setSelectedUserId] = useState(null)
  const [messagesState, setMessagesState] = useState([])
  const [loading, setLoading] = useState(false)
  const [sending, setSending] = useState(false)
  const [content, setContent] = useState('')
  const scrollRef = useRef(null)

  const me = currentUser

  const otherUsers = useMemo(() => {
    return (users || []).filter(user => user.id !== me?.id)
  }, [users, me])

  useEffect(() => {
    if (!selectedUserId && otherUsers.length) {
      setSelectedUserId(otherUsers[0].id)
    }
  }, [otherUsers, selectedUserId])

  useEffect(() => {
    if (me?.id && selectedUserId) {
      loadConversation(selectedUserId)
    } else {
      setMessagesState([])
    }
  }, [me?.id, selectedUserId])

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [messagesState])

  async function loadConversation(userId) {
    setLoading(true)
    try {
      const data = await fetchMessagesWith(userId)
      setMessagesState(data || [])
      const unread = (data || []).filter(item => !item.isRead && item.receiverId === me?.id)
      if (unread.length) {
        await Promise.all(unread.map(item => markMessageRead(item.id).catch(() => null)))
      }
    } catch (e) {
      message.error(e.message || 'Không thể tải hội thoại')
    } finally {
      setLoading(false)
    }
  }

  async function handleSend() {
    if (!content.trim()) {
      return
    }
    setSending(true)
    try {
      await sendMessageTo(selectedUserId, content.trim())
      setContent('')
      await loadConversation(selectedUserId)
    } catch (e) {
      message.error(e.message || 'Không gửi được tin nhắn')
    } finally {
      setSending(false)
    }
  }

  if (!me) {
    return <Card>Đăng nhập để sử dụng nhắn tin.</Card>
  }

  return (
    <Space align="start" style={{ width: '100%' }} size={16}>
      <Card title="Danh sách" style={{ width: 280, flexShrink: 0 }}>
        <List
          dataSource={otherUsers}
          locale={{ emptyText: 'Chưa có người dùng khác' }}
          renderItem={(user) => (
            <List.Item
              onClick={() => setSelectedUserId(user.id)}
              style={{
                cursor: 'pointer',
                background: selectedUserId === user.id ? '#e6f4ff' : undefined,
                borderRadius: 8,
                padding: 12,
                marginBottom: 4
              }}
            >
              <List.Item.Meta
                avatar={<Avatar src={user.avatarUrl}>{user.username?.[0]}</Avatar>}
                title={<span>{user.username}</span>}
                description={user.email}
              />
            </List.Item>
          )}
        />
      </Card>

      <Card
        title={selectedUserId ? `Trò chuyện với ${users.find(u => u.id === selectedUserId)?.username || ''}` : 'Chọn người để trò chuyện'}
        style={{ flex: 1, minHeight: 520, display: 'flex', flexDirection: 'column' }}
        loading={loading}
      >
        <div
          ref={scrollRef}
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: 16,
            background: '#f5f5f5',
            borderRadius: 8,
            marginBottom: 16
          }}
        >
          {messagesState.length === 0 && !loading ? (
            <Empty description="Chưa có tin nhắn" />
          ) : (
            messagesState.map(msg => {
              const isMine = msg.senderId === me.id
              return (
                <div
                  key={msg.id}
                  style={{
                    display: 'flex',
                    justifyContent: isMine ? 'flex-end' : 'flex-start',
                    marginBottom: 12
                  }}
                >
                  <div
                    style={{
                      maxWidth: '70%',
                      background: isMine ? '#1677ff' : '#fff',
                      color: isMine ? '#fff' : '#000',
                      padding: '10px 14px',
                      borderRadius: 16,
                      borderBottomRightRadius: isMine ? 2 : 16,
                      borderBottomLeftRadius: isMine ? 16 : 2,
                      boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
                    }}
                  >
                    <Typography.Text style={{ color: isMine ? '#fff' : '#000' }}>
                      {msg.content}
                    </Typography.Text>
                    <div style={{ fontSize: 11, marginTop: 6, opacity: 0.8 }}>
                      {new Date(msg.sentAt).toLocaleString()} {isMine && msg.isRead ? '✓✓' : ''}
                    </div>
                  </div>
                </div>
              )
            })
          )}
        </div>

        <Space.Compact style={{ width: '100%' }}>
          <TextArea
            placeholder="Nhập tin nhắn..."
            value={content}
            onChange={e => setContent(e.target.value)}
            autoSize={{ minRows: 2, maxRows: 4 }}
            disabled={!selectedUserId}
          />
          <Button
            type="primary"
            onClick={handleSend}
            loading={sending}
            disabled={!selectedUserId || !content.trim()}
          >
            Gửi
          </Button>
        </Space.Compact>
      </Card>
    </Space>
  )
}


