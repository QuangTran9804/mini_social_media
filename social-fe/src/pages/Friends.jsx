import { useEffect, useMemo, useState } from 'react'
import { Button, Card, Flex, Input, List, Space, Tabs, Tag, message } from 'antd'
import {
  acceptRequest, cancelRequest, inboxRequests, listFriends, rejectRequest, sendRequest, sentRequests, fetchUsers, unfriend
} from '../api/client.js'

export default function Friends({ currentUser, knownUsers = [] }) {
  const [loading, setLoading] = useState(false)
  const [friends, setFriends] = useState([])
  const [inbox, setInbox] = useState([])
  const [sent, setSent] = useState([])
  const [users, setUsers] = useState(knownUsers)
  const [filter, setFilter] = useState('')

  const currentUserId = currentUser?.id

  useEffect(() => {
    if (knownUsers?.length) {
      setUsers(knownUsers)
    }
  }, [knownUsers])

  async function refreshAll() {
    if (!currentUserId) return
    setLoading(true)
    try {
      const [f, ib, st, us] = await Promise.all([
        listFriends(),
        inboxRequests(),
        sentRequests(),
        users.length ? Promise.resolve(users) : fetchUsers()
      ])
      setFriends(f || [])
      setInbox(ib || [])
      setSent(st || [])
      if (!users.length) setUsers(us || [])
    } catch (e) {
      message.error(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refreshAll()
  }, [currentUserId])

  const allOtherUsers = useMemo(() => {
    const lower = filter.toLowerCase()
    return (users || [])
      .filter(u => u.id !== currentUserId)
      .filter(u => !lower || u.username.toLowerCase().includes(lower) || u.email.toLowerCase().includes(lower))
      .sort((a, b) => a.username.localeCompare(b.username))
  }, [users, currentUserId, filter])

  function isFriend(userId) {
    return friends.some(f => f.friend?.id === userId)
  }

  function hasPendingBetween(userId) {
    return inbox.some(r => r.sender?.id === userId) || sent.some(r => r.receiver?.id === userId && r.status === 'PENDING')
  }

  async function onSend(toUserId) {
    try {
      await sendRequest(toUserId)
      message.success('Đã gửi lời mời')
      await refreshAll()
    } catch (e) {
      message.error(e.message)
    }
  }

  async function onCancel(reqId) {
    try {
      await cancelRequest(reqId)
      message.success('Đã huỷ lời mời')
      await refreshAll()
    } catch (e) {
      message.error(e.message)
    }
  }

  async function onAccept(reqId) {
    try {
      await acceptRequest(reqId)
      message.success('Đã chấp nhận')
      await refreshAll()
    } catch (e) {
      message.error(e.message)
    }
  }

  async function onReject(reqId) {
    try {
      await rejectRequest(reqId)
      message.success('Đã từ chối')
      await refreshAll()
    } catch (e) {
      message.error(e.message)
    }
  }

  async function handleUnfriend(friendId) {
    try {
      await unfriend(friendId)
      message.success('Đã gỡ bạn')
      await refreshAll()
    } catch (e) {
      message.error(e.message)
    }
  }

  // Seed UI removed per request

  if (!currentUserId) {
    return <Card title="Kết bạn">Đăng nhập để quản lý bạn bè.</Card>
  }

  return (
    <Card
      title="Kết bạn"
      loading={loading && !friends.length && !inbox.length && !sent.length}
    >
      <Tabs
        items={[
          {
            key: 'friends',
            label: 'Bạn bè',
            children: (
              <List
                dataSource={friends}
                locale={{ emptyText: 'Chưa có bạn' }}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <Button danger onClick={() => handleUnfriend(item.friend?.id)}>
                        Gỡ bạn
                      </Button>
                    ]}
                  >
                    <List.Item.Meta
                      title={<span>{item.friend?.username}</span>}
                      description={item.friend?.email}
                    />
                    <Tag color="green">Friend</Tag>
                  </List.Item>
                )}
              />
            )
          },
          {
            key: 'inbox',
            label: `Lời mời đến (${inbox.length})`,
            children: (
              <List
                dataSource={inbox}
                locale={{ emptyText: 'Không có lời mời' }}
                renderItem={(req) => (
                  <List.Item
                    actions={[
                      <Space>
                        <Button type="primary" onClick={() => onAccept(req.id)}>Chấp nhận</Button>
                        <Button danger onClick={() => onReject(req.id)}>Từ chối</Button>
                      </Space>
                    ]}
                  >
                    <List.Item.Meta
                      title={`${req.sender?.username}`}
                      description={`${req.sender?.email}`}
                    />
                    <Tag color="gold">PENDING</Tag>
                  </List.Item>
                )}
              />
            )
          },
          {
            key: 'sent',
            label: `Đã gửi (${sent.length})`,
            children: (
              <List
                dataSource={sent}
                locale={{ emptyText: 'Không có lời mời đã gửi' }}
                renderItem={(req) => (
                  <List.Item
                    actions={[
                      req.status === 'PENDING'
                        ? <Button danger onClick={() => onCancel(req.id)}>Huỷ</Button>
                        : <Tag color={req.status === 'ACCEPTED' ? 'green' : 'red'}>{req.status}</Tag>
                    ]}
                  >
                    <List.Item.Meta
                      title={`${req.receiver?.username}`}
                      description={`${req.receiver?.email}`}
                    />
                    {req.status === 'PENDING' && <Tag color="gold">PENDING</Tag>}
                  </List.Item>
                )}
              />
            )
          },
          {
            key: 'explore',
            label: 'Khám phá người dùng',
            children: (
              <div>
                <Flex gap={12} style={{ marginBottom: 16 }}>
                  <Input placeholder="Tìm theo tên hoặc email..." value={filter} onChange={e => setFilter(e.target.value)} />
                </Flex>
                <List
                  dataSource={allOtherUsers}
                  renderItem={(u) => {
                    const friend = isFriend(u.id)
                    const pending = hasPendingBetween(u.id)
                    return (
                      <List.Item
                        actions={[
                          <Button
                            disabled={friend || pending}
                            onClick={() => onSend(u.id)}
                          >
                            {friend ? 'Đã là bạn' : pending ? 'Đang chờ' : 'Kết bạn'}
                          </Button>
                        ]}
                      >
                        <List.Item.Meta
                          title={u.username}
                          description={u.email}
                        />
                        {friend && <Tag color="green">Friend</Tag>}
                        {!friend && pending && <Tag color="gold">Pending</Tag>}
                      </List.Item>
                    )
                  }}
                />
              </div>
            )
          }
        ]}
      />
    </Card>
  )
}


