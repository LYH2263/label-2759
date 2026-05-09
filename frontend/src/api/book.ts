import request from '../utils/request'

export interface Book {
  id?: number
  title: string
  author: string
  isbn: string
  category: string
  price: number
  stock: number
  description: string
  createTime?: string
  updateTime?: string
}

export function getBookList(params: any) {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

export function getBook(id: number) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

export function createBook(data: Book) {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

export function updateBook(data: Book) {
  return request({
    url: '/books',
    method: 'put',
    data
  })
}

export function deleteBook(id: number) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}
