export interface Room {
  roomId: string;
  roomType: string;
  roomNumber:number;
  isAcAvailable: boolean;
  withFood: boolean;
  capacity: number;
  currentCapacity:number;
  price: number;
  floorNumber: number;
  roommateDTO: Roommate[];
}

export interface AvailabilityRequest {
  roomType: string;
  withAC: boolean;
  withFood: boolean;
  capacity: number;
}

export interface BookingRequest {
  username: string;
  password: string;
  email: string;
  gender: string;
  withFood: boolean;
  checkInDate: string;
  checkOutDate: string;
  referralId:string;
}
export interface Payment {
  id: number; // Assuming `id` is a number in the frontend
  amount: number; // Assuming `amount` is a number
  paymentStatus: string;
  paymentDate: string; // Use `string` for dates in the frontend (or use `Date` if you prefer)
  transactionId: string;
  paymentMethod: string;
  username: string;
}

export interface Roommate {
  roommateId: number;
  username: string;
  email: string;
  rentStatus: string;
  roomNumber: string;
  gender: string;
  rentAmount:number;
  checkInDate:string;
  checkOutDate:string;
  withFood: boolean;
  referralId:string;
  referralCount:number;
  paymentList:Payment[];
  referralDetailsList:ReferralDetail[];
}

export interface ReferralDetail {
  username: string;
  referralDate: string;
}

export interface RoommateUpdateRequest {
  roommateId: number;
  username: string;
  password: string;
  email: string;
}

export interface PaymentDetails {
  id: number;
  username: string;
  amount: number;
  paymentDate: string;
  paymentStatus: string;
  paymentMethod: string;
  roomNumber:string;
  transactionId:string;
}
export interface paymentData {
  orderId: string;
  paymentId:string;
  email:String;
}

export interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description: string;
  order_id: string;
  handler: (response: any) => void;
  prefill: {
    name: string;
    email: string;
  };
  notes: {
    address: string;
  };
  theme: {
    color: string;
  };
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface Payment {
  id: number;
  amount: number;
  date: string;
  status: string;
}

export interface ReferralDetails {
  id: number;
  referredUser: string;
  date: string;
}

export interface RazorpayResponse {
  razorpay_payment_id: string;
  razorpay_order_id: string;
  razorpay_signature: string;
}

export interface OwnerLoginRequest {
  ownerName: string;
  password: string;
}

export interface UpdateRoommateDetails{
  username: string
  password: string
  email: string
  withFood: boolean
  checkOutDate: string
}

export interface vacateRequest{
   vacateReason:string
    checkOutDate: string
}
export interface Grievance{
  grievanceContent: string
  grievanceFrom:string

}

export interface VacateRequestList {
  vacateRequestId: string;
  roommateName: string;
  roomNumber: string;
  checkOutDate: string;
  vacateReason: string;
  isRead: boolean;
  createdAt:string;
}

export interface GrievanceList {
  grievanceId: string;
  roommateName: string;
  roomNumber: string;
  createdAt: string;
  grievanceContent: string;
  isRead: boolean;
}

export interface RoomRequest {
  floorNumber: number;

  roomType: string;
  capacity: number;
  currentCapacity: number;
  isAcAvailable: boolean;
  price: number;
}
