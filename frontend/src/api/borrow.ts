import request from '../utils/request'

export interface BorrowRecord {
  id?: number
  userId: number
  bookId: number
  borrowDate: string
  returnDate?: string
  dueDate: string
  status: number
  bookTitle?: string
  username?: string
}

export function borrowBook(bookId: number) {
  return request({
    url: `/borrow/${bookId}`,
    method: 'post'
  })
}

export function returnBook(id: number) {
  return request({
    url: `/borrow/return/${id}`,
    method: 'post'
  })
}

export function getMyBorrowRecords(params: any) {
  return request({
    url: '/borrow/my',
    method: 'get',
    params
  })
}

export function getAllBorrowRecords(params: any) {
  return request({
    url: '/borrow/list',
    method: 'get',
    params
  })
}
