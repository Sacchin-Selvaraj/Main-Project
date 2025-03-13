import axios from 'axios';
import { AvailabilityRequest, BookingRequest, Room, Roommate, RoommateUpdateRequest, PaymentDetails, paymentData,LoginRequest,OwnerLoginRequest
  ,UpdateRoommateDetails,vacateRequest,Grievance,GrievanceList,VacateRequestList,RoomRequest
 } from './types';

const API_BASE_URL = 'http://localhost:8090';

export const api = {
  // Room endpoints
  getAllRooms: () => 
    axios.get<Room[]>(`${API_BASE_URL}/room/all-rooms`),
  
  getRoom: (roomId: string) =>
    axios.get<Room>(`${API_BASE_URL}/room/get-room/${roomId}`),
  
  checkAvailability: (request: AvailabilityRequest) =>
    axios.post(`${API_BASE_URL}/room/check-availability`, request),
  
  bookRoom: (roomId: string, request: BookingRequest) =>
    axios.post(`${API_BASE_URL}/room/book/${roomId}`, request),

  createPaymentOrder : (request: paymentData) =>
     axios.post(`${API_BASE_URL}/payments/payrent`, request),

  // Roommate endpoints
  getAllRoommates: () =>
    axios.get<Roommate[]>(`${API_BASE_URL}/roommate/all-roommates`),

  getRoommate: (request: LoginRequest) =>
    axios.post(`${API_BASE_URL}/roommate/get-roommate`, request, {
      headers: {
        'Content-Type': 'application/json',
      }
    }),

  updateRoommate: (roommateId: number, request: RoommateUpdateRequest) =>
    axios.put(`${API_BASE_URL}/roommate/update/${roommateId}`, request),

  vacateRoommate: (username: string) =>
    axios.delete(`${API_BASE_URL}/roommate/vacate/${username}`),

  getRentPendingRoommates: () =>
    axios.get<Roommate[]>(`${API_BASE_URL}/roommate/rentpending`),

  // Notification endpoints
  sendEmail: () =>
    axios.get(`${API_BASE_URL}/notification/send-mail`),

  sendRentPendingNotification: () =>
    axios.get(`${API_BASE_URL}/notification/send-rent-pending`),

  // Payment endpoints
  getPaymentDetails: () =>
    axios.get<PaymentDetails[]>(`${API_BASE_URL}/payments/paymentDetails`),

  payRent: (username: string) =>
    axios.post(`${API_BASE_URL}/payments/payrent`, { username }),


  createPaymentCallback: (request: paymentData) =>
    axios.post(`${API_BASE_URL}/payments/paymentCallback`, request),

  verifyOwner: (request: OwnerLoginRequest) =>
    axios.post(`${API_BASE_URL}/owner/login`, request),

  updateRoommateDetails: (roommateId: number, request: UpdateRoommateDetails) =>
    axios.patch(`${API_BASE_URL}/roommate/update-details/${roommateId}`, request),

  sendVacateRequest: (roommateId: number, request: vacateRequest) =>
    axios.post(`${API_BASE_URL}/roommate/send-vacate-request/${roommateId}`, request),

  raiseGrievance: (roommateId: number, request: Grievance) =>
    axios.post(`${API_BASE_URL}/grievance/raise/${roommateId}`, request),

  getPendingVacateRequests: () => axios.get<VacateRequestList[]>(`${API_BASE_URL}/roommate/pending-vacate-request`),

  markVacateRequestAsRead: (vacateRequestId: string) => 
    axios.put(`${API_BASE_URL}/roommate/mark-read/${vacateRequestId}`),

  getPendingGrievances: () => axios.get<GrievanceList[]>(`${API_BASE_URL}/grievance/pending-grievance`),

  markGrievanceAsRead: (grievanceId: string) => 
    axios.post(`${API_BASE_URL}/grievance/mark-as-read/${grievanceId}`),

  addRoom: (roomData: RoomRequest) => 
    axios.post(`${API_BASE_URL}/room/add-room`, roomData),

  editRoom: (roomId: string, roomData: Partial<RoomRequest>) => 
    axios.patch(`${API_BASE_URL}/room/edit-room/${roomId}`, roomData),
    
  deleteRoom: (roomId: string) => 
    axios.delete(`${API_BASE_URL}/room/delete-room/${roomId}`),

  getPayments: (params: any) =>
    axios.get(`${API_BASE_URL}/payments/sort`, { params }),
  
};