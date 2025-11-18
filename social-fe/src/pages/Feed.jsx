import { useEffect, useMemo, useState } from 'react'
import { Avatar, Button, Card, Divider, Form, Input, Space, Spin, Tag, message } from 'antd'
import { createPost, fetchFeed, toggleReaction } from '../api/client.js'

const reactionOptions = [
  { key: 'LIKE', label: 'Thích', emoji: '👍' },
  { key: 'LOVE', label: 'Yêu thích', emoji: '❤️' },
  { key: 'LAUGH', label: 'Haha', emoji: '😆' },
  { key: 'WOW', label: 'Wow', emoji: '😮' },
  { key: 'SAD', label: 'Buồn', emoji: '😢' },
  { key: 'ANGRY', label: 'Phẫn nộ', emoji: '😡' }
]

export default function Feed({ currentUser }) {
  const [loading, setLoading] = useState(false)
  const [creating, setCreating] = useState(false)
  const [posts, setPosts] = useState([])
  const [form] = Form.useForm()

  const hasUser = !!currentUser?.id

  useEffect(() => {
    if (hasUser) {
      loadFeed()
    } else {
      setPosts([])
    }
  }, [hasUser])

  async function loadFeed() {
    setLoading(true)
    try {
      const data = await fetchFeed()
      setPosts(data || [])
    } catch (e) {
      message.error(e.message || 'Không tải được bảng tin')
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(values) {
    if (!values.content && !values.imageUrl) {
      message.warning('Viết gì đó trước khi đăng nhé!')
      return
    }
    setCreating(true)
    try {
      await createPost(values)
      message.success('Đăng bài thành công')
      form.resetFields()
      await loadFeed()
    } catch (e) {
      message.error(e.message || 'Không thể đăng bài')
    } finally {
      setCreating(false)
    }
  }

  async function handleReaction(postId, reactionKey) {
    try {
      await toggleReaction(postId, reactionKey)
      await loadFeed()
    } catch (e) {
      message.error(e.message || 'Không thể cập nhật cảm xúc')
    }
  }

  const content = useMemo(() => {
    if (!hasUser) {
      return <Card>Hãy đăng nhập để xem bảng tin nhé.</Card>
    }

    if (loading) {
      return (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin />
        </div>
      )
    }

    if (!posts.length) {
      return <Card>Chưa có bài viết nào. Hãy là người đầu tiên đăng nhé!</Card>
    }

    return posts.map(post => (
      <Card
        key={post.id}
        style={{ marginBottom: 16 }}
        bodyStyle={{ padding: 20 }}
      >
        <Space align="start" style={{ width: '100%' }}>
          <Avatar size={48} src={post.author?.avatarUrl}>
            {post.author?.username?.[0]}
          </Avatar>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <strong>{post.author?.username}</strong>
                <div style={{ fontSize: 12, color: '#888' }}>
                  {new Date(post.createdAt).toLocaleString()}
                </div>
              </div>
            </div>
            <div style={{ marginTop: 12, whiteSpace: 'pre-wrap', fontSize: 15 }}>
              {post.content}
            </div>
            {post.imageUrl && (
              <div style={{ marginTop: 12 }}>
                <img
                  src={post.imageUrl}
                  alt="post"
                  style={{ width: '100%', borderRadius: 12, maxHeight: 360, objectFit: 'cover' }}
                />
              </div>
            )}
            <Divider style={{ margin: '16px 0' }} />
            <Space wrap>
              {reactionOptions.map(option => (
                <Button
                  key={option.key}
                  size="middle"
                  type={post.viewerReaction === option.key ? 'primary' : 'default'}
                  onClick={() => handleReaction(post.id, option.key)}
                >
                  <span style={{ marginRight: 6 }}>{option.emoji}</span>
                  {option.label}
                  <span style={{ marginLeft: 6, color: 'rgba(0,0,0,0.45)' }}>
                    {post.reactions?.[option.key] || 0}
                  </span>
                </Button>
              ))}
            </Space>
            <div style={{ marginTop: 12 }}>
              <Space size="small">
                <strong>Tổng: {post.totalLikes}</strong>
                <Space size="small">
                  {reactionOptions
                    .filter(option => (post.reactions?.[option.key] || 0) > 0)
                    .map(option => (
                      <Tag key={option.key} color="blue">
                        {option.emoji} {post.reactions?.[option.key]}
                      </Tag>
                    ))}
                </Space>
              </Space>
            </div>
          </div>
        </Space>
      </Card>
    ))
  }, [hasUser, loading, posts])

  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <Card title="Đăng bài mới" style={{ background: '#fafafa' }}>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="content" label="Nội dung">
            <Input.TextArea
              placeholder="Hôm nay bạn nghĩ gì?"
              autoSize={{ minRows: 3, maxRows: 6 }}
              allowClear
              disabled={!hasUser}
            />
          </Form.Item>
          <Form.Item name="imageUrl" label="Link hình ảnh (tuỳ chọn)">
            <Input placeholder="https://example.com/photo.jpg" allowClear disabled={!hasUser} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={creating} disabled={!hasUser}>
              Đăng bài
            </Button>
          </Form.Item>
        </Form>
      </Card>
      {content}
    </Space>
  )
}


