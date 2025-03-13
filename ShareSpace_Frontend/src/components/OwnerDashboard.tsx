import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { Room, Roommate, PaymentDetails, VacateRequestList, GrievanceList,RoomRequest } from '../types';
import { Building, Users, CreditCard, Mail, RefreshCw, X, FileText, MessageSquare, LogOut, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import axios from 'axios';

const OwnerDashboard: React.FC = () => {
  const navigate = useNavigate();
  const isMounted = useRef(false);
  const [activeTab, setActiveTab] = useState('rooms');
  const [rooms, setRooms] = useState<Room[]>([]);
  const [roommates, setRoommates] = useState<Roommate[]>([]);
  const [payments, setPayments] = useState<PaymentDetails[]>([]);
  const [vacateRequests, setVacateRequests] = useState<VacateRequestList[]>([]);
  const [grievances, setGrievances] = useState<GrievanceList[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabLoading, setTabLoading] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);
  const [showVacateModal, setShowVacateModal] = useState(false);
  const [selectedRoommate, setSelectedRoommate] = useState<Roommate | null>(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isSendingRentNotification, setIsSendingRentNotification] = useState(false);
  const [isSendingNotification, setIsSendingNotification] = useState(false);
  const [sortField, setSortField] = useState<string>('username');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [filterRentStatus, setFilterRentStatus] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [itemsPerPage] = useState<number>(6);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [roommatesLoading, setRoommatesLoading] = useState<boolean>(true);
  const [paymentSortField, setPaymentSortField] = useState('paymentDate');
  const [paymentSortOrder, setPaymentSortOrder] = useState('desc');
  const [selectedPaymentDate, setSelectedPaymentDate] = useState<string>('');
  const [paymentCurrentPage, setPaymentCurrentPage] = useState<number>(0);
  const [paymentItemsPerPage] = useState<number>(6);
  const [paymentTotalPages, setPaymentTotalPages] = useState<number>(1);
  const [paymentsLoading, setPaymentsLoading] = useState<boolean>(true);
  const [searchUsername, setSearchUsername] = useState<string>('');
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [showAddRoomModal, setShowAddRoomModal] = useState(false);
  const [newRoomData, setNewRoomData] = useState({
    floorNumber: 0,
    roomNumber: '',
    roomType: '',
    capacity: 0,
    currentCapacity: 0,
    isAcAvailable: false,
    price: 0
  });
  const [showEditRoomModal, setShowEditRoomModal] = useState(false);
  const [editRoomData, setEditRoomData] = useState<Partial<RoomRequest>>({});
  const [selectedRoomId, setSelectedRoomId] = useState<string>('');
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);
  const [roomToDelete, setRoomToDelete] = useState<string>('');

  // Single fetch function for each data type
  const fetchRooms = async () => {
    try {
      
      const response = await api.getAllRooms();

      setRooms(response.data || []);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Error fetching rooms data';
      toast.error(errorMessage);
    }
  };

  const fetchVacateRequests = async () => {
    try {
      const response = await api.getPendingVacateRequests();
      setVacateRequests(response.data || []);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Error fetching vacate requests';
      toast.error(errorMessage);
    }
  };

  const fetchGrievances = async () => {
    try {
      const response = await api.getPendingGrievances();
      setGrievances(response.data || []);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Error fetching grievances';
      toast.error(errorMessage);
    }
  };

  const fetchSortedRoommates = async () => {
    try {
      setRoommatesLoading(true);
      const params = new URLSearchParams({
        page: currentPage.toString(),
        limit: itemsPerPage.toString(),
        sortField,
        sortOrder
      });

      if (filterRentStatus) {
        params.append('rentStatus', filterRentStatus);
      }

      const response = await axios.post(`http://localhost:8090/roommate/sort?${params.toString()}`);

      if (response.data) {
        setRoommates(response.data.content || []);
        setTotalPages(response.data.totalPages || 1);
      } else {
        setRoommates([]);
        setTotalPages(1);
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to fetch roommates data';
      toast.error(errorMessage);
      setRoommates([]);
      setTotalPages(1);
    } finally {
      setRoommatesLoading(false);
    }
  };

  const fetchSortedPayments = async (params = {
    page: paymentCurrentPage,
    limit: 10,
    sortField: paymentSortField,
    sortOrder: paymentSortOrder,
    ...(searchUsername && { username: searchUsername }),
    ...(selectedPaymentDate && { paymentDate: selectedPaymentDate })
  }) => {
    try {
      setPaymentsLoading(true);
      const response = await api.getPayments(params);
      setPayments(response.data.content);
      setPaymentTotalPages(response.data.totalPages);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to fetch payments');
      setPayments([]);
      setPaymentTotalPages(1);
    } finally {
      setPaymentsLoading(false);
    }
  };

  const handleSearchPayments = async (username: string) => {
    if (!username.trim()) {
      fetchSortedPayments();
      return;
    }

    try {
      setIsSearching(true);
      setPaymentsLoading(true);
      const response = await axios.get(`http://localhost:8090/payments/search/${username}`);
      
      if (response.data) {
        setPayments(response.data);
        setPaymentTotalPages(1);
      } else {
        setPayments([]);
        setPaymentTotalPages(1);
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'No payments found for this username';
      toast.error(errorMessage);
      setPayments([]);
      setPaymentTotalPages(1);
    } finally {
      setPaymentsLoading(false);
      setIsSearching(false);
    }
  };

  // Initial data load - only fetch rooms
  useEffect(() => {
    const loadInitialData = async () => {
      if (!isMounted.current) {
        setLoading(true);
        
        await fetchRooms();

        setLoading(false);
        isMounted.current = true;
      }
    };

    loadInitialData();
    
    return () => {
      isMounted.current = false;
    };
  }, []);

  // Handle tab changes with lazy loading
  const handleTabChange = async (tabId: string) => {
    if (tabId === activeTab) return; // Prevent unnecessary tab changes
    
    setActiveTab(tabId);
    setTabLoading(true);
    if (loading) return;

    try {
      switch (tabId) {
        case 'rooms':
          if (rooms.length === 0) {
            await fetchRooms();
          }
          break;
        case 'roommates':
          await fetchSortedRoommates();
          break;
        case 'payments':
          if (payments.length === 0) {
            await fetchSortedPayments();
          }
          break;
        case 'vacateRequests':
          if (vacateRequests.length === 0) {
            await fetchVacateRequests();
          }
          break;
        case 'grievances':
          if (grievances.length === 0) {
            await fetchGrievances();
          }
          break;
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          `Error loading ${tabId} data`;
      toast.error(errorMessage);
    } finally {
      setTabLoading(false);
    }
  };

  // Update the refresh handler to handle errors properly
  const handleRefresh = async () => {
    if (isRefreshing) return;
    
    setIsRefreshing(true);
    try {
      switch (activeTab) {
        case 'rooms':
          await fetchRooms();
          break;
        case 'roommates':
          await fetchSortedRoommates();
          break;
        case 'payments':
          await fetchSortedPayments();
          break;
        case 'vacateRequests':
          await fetchVacateRequests();
          break;
        case 'grievances':
          await fetchGrievances();
          break;
      }
      toast.success('Data refreshed successfully');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Error refreshing data';
      toast.error(errorMessage);
    } finally {
      setIsRefreshing(false);
    }
  };

  // Room click handler without navigation
  const handleRoomClick = useCallback((room: Room, e: React.MouseEvent) => {
    // Prevent default behavior to avoid navigation
    e.preventDefault();
    e.stopPropagation();
    setSelectedRoom(room);
  }, []);

  const handleCloseRoomDetails = () => {
    setSelectedRoom(null);
  };

  const handleSendEmail = async () => {
    if (isSendingNotification) return;
    
    setIsSendingNotification(true);
    try {
      await api.sendEmail();
      toast.success('Email notification sent successfully');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to send email notification';
      toast.error(errorMessage);
    } finally {
      setIsSendingNotification(false);
    }
  };

  const handleSendRentPendingNotification = async () => {
    if (isSendingNotification) return; // Prevent multiple clicks
    
    setIsSendingNotification(true);
    try {
      await api.sendRentPendingNotification();
      toast.success('Rent pending notifications sent successfully');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to send rent pending notifications';
      toast.error(errorMessage);
    } finally {
      setIsSendingNotification(false);
    }
  };

  const handleVacateClick = async (roommate: Roommate) => {
    try {
      setSelectedRoommate(roommate);
      setShowVacateModal(true);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to process vacate request';
      toast.error(errorMessage);
    }
  };

  const handleConfirmVacate = async () => {
    if (!selectedRoommate) return;

    try {
      await api.vacateRoommate(selectedRoommate.username);
      toast.success('Roommate vacated successfully');
      
      setRoommates(prevRoommates => 
        prevRoommates.filter(r => r.roommateId !== selectedRoommate.roommateId)
      );
      
      setRooms(prevRooms => 
        prevRooms.map(room => ({
          ...room,
          roommateList: room.roommateDTO?.filter(
            r => r.roommateId !== selectedRoommate.roommateId
          ) || []
        }))
      );
  
      if (selectedRoom) {
        setSelectedRoom(prevRoom => {
          if (!prevRoom) return null;
          return {
            ...prevRoom,
            roommateList: prevRoom.roommateDTO?.filter(
              r => r.roommateId !== selectedRoommate.roommateId
            ) || [],
            currentCapacity: (prevRoom.currentCapacity || 0) - 1
          };
        });
      }
  
      setShowVacateModal(false);
      setSelectedRoommate(null);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to vacate roommate';
      toast.error(errorMessage);
    }
  };

  const handleMarkVacateRequestAsRead = async (vacateRequestId: string) => {
    try {
      await api.markVacateRequestAsRead(vacateRequestId);
      setVacateRequests(prev => prev.filter(request => request.vacateRequestId !== vacateRequestId));
      toast.success('Vacate request marked as read');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to mark vacate request as read';
      toast.error(errorMessage);
    }
  };

  const handleMarkGrievanceAsRead = async (grievanceId: string) => {
    try {
      await api.markGrievanceAsRead(grievanceId);
      setGrievances(prev => prev.filter(grievance => grievance.grievanceId !== grievanceId));
      toast.success('Grievance marked as read');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to mark grievance as read';
      toast.error(errorMessage);
    }
  };

  // Instead, add specific handlers for pagination, sorting, and filtering
  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    fetchSortedRoommates();
  };

  const handleSortChange = (field: string) => {
    // If clicking the same field, toggle order. If new field, default to asc
    const newOrder = field === sortField ? (sortOrder === 'asc' ? 'desc' : 'asc') : 'asc';
    setSortField(field);
    setSortOrder(newOrder);
    setCurrentPage(0);
    
    try {
      setRoommatesLoading(true);
      const params = new URLSearchParams({
        page: '0',
        limit: itemsPerPage.toString(),
        sortField: field,
        sortOrder: newOrder
      });

      if (filterRentStatus) {
        params.append('rentStatus', filterRentStatus);
      }

      axios.post(`http://localhost:8090/roommate/sort?${params.toString()}`)
        .then(response => {
          if (response.data) {
            setRoommates(response.data.content || []);
            setTotalPages(response.data.totalPages || 1);
          }
        })
        .catch(error => {
          toast.error(error.response?.data?.message || 'Failed to sort roommates');
        })
        .finally(() => {
          setRoommatesLoading(false);
        });
    } catch (error: any) {
      toast.error('Failed to process sorting');
      setRoommatesLoading(false);
    }
  };

  const handleFilterChange = (status: string) => {
    setFilterRentStatus(status);
    setCurrentPage(0);
    
    try {
      setRoommatesLoading(true);
      const params = new URLSearchParams({
        page: '0',
        limit: itemsPerPage.toString(),
        sortField,
        sortOrder
      });

      // Only append rentStatus if a status is selected
      if (status) {
        params.append('rentStatus', status);
      }

      axios.post(`http://localhost:8090/roommate/sort?${params.toString()}`)
        .then(response => {
          if (response.data) {
            setRoommates(response.data.content || []);
            setTotalPages(response.data.totalPages || 1);
          }
        })
        .catch(error => {
          toast.error(error.response?.data?.message || 'Failed to filter roommates');
        })
        .finally(() => {
          setRoommatesLoading(false);
        });
    } catch (error: any) {
      toast.error('Failed to process filtering');
      setRoommatesLoading(false);
    }
  };

  const handleAddRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.addRoom(newRoomData);
      toast.success('Room added successfully');
      setShowAddRoomModal(false);
       fetchRooms(); // Refresh rooms list
      setNewRoomData({
        floorNumber: 0,
        roomNumber: '',
        roomType: '',
        capacity: 0,
        currentCapacity: 0,
        isAcAvailable: false,
        price:0
      });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to add room';
      toast.error(errorMessage);
    }
  };

  const handleEditRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.editRoom(selectedRoomId, editRoomData);
      toast.success('Room updated successfully');
      setShowEditRoomModal(false);
      fetchRooms(); // Refresh rooms list
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to update room';
      toast.error(errorMessage);
    }
  };

  const handleDeleteRoom = async (roomId: string) => {
    setRoomToDelete(roomId);
    setShowDeleteConfirmation(true);
  };

  const handleConfirmDelete = async () => {
    try {
      await api.deleteRoom(roomToDelete);
      toast.success('Room deleted successfully');
      fetchRooms(); // Refresh rooms list
      setShowDeleteConfirmation(false);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to delete room';
      toast.error(errorMessage);
    }
  };

  // Update the sorting handler to prevent double requests
  const handlePaymentSort = (field: string) => {
    const newOrder = paymentSortField === field && paymentSortOrder === 'asc' ? 'desc' : 'asc';
    setPaymentSortOrder(newOrder);
    setPaymentSortField(field);
    setPaymentCurrentPage(0); // Reset to first page when sorting
    
    // Fetch with new sort parameters
    const params = {
      page: 0, // Reset to first page
      limit: 6,
      sortField: field,
      sortOrder: newOrder,
      ...(searchUsername && { username: searchUsername }),
      ...(selectedPaymentDate && { paymentDate: selectedPaymentDate })
    };
    
    fetchSortedPayments(params);
  };

  // Update the pagination handler to prevent double requests
  const handlePaymentPageChange = (newPage: number) => {
    setPaymentCurrentPage(newPage);
    
    // Fetch with current parameters
    const params = {
      page: newPage,
      limit: 6,
      sortField: paymentSortField,
      sortOrder: paymentSortOrder,
      ...(searchUsername && { username: searchUsername }),
      ...(selectedPaymentDate && { paymentDate: selectedPaymentDate })
    };
    
    fetchSortedPayments(params);
  };

  useEffect(() => {
    if (activeTab === 'roommates') {
     // fetchSortedRoommates();
    }
  }, [activeTab, sortField, sortOrder, filterRentStatus, currentPage]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a] 
        flex items-center justify-center p-4">
        <div className="w-16 h-16 border-4 border-purple-500/20 border-t-purple-500 
          rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a]">
      <div className="p-6 md:p-10 max-w-[1400px] mx-auto">
        {/* Header Section - Reduced font size */}
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8 mb-8
          border border-purple-500/20 shadow-lg shadow-purple-500/5">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div className="space-y-2">
              <h1 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-purple-400 to-purple-600 
                bg-clip-text text-transparent">
                Owner Dashboard
              </h1>
              <p className="text-purple-300/60 text-base">
                Manage your property and roommates
              </p>
            </div>
            <div className="flex items-center gap-4">
              <button
                onClick={handleRefresh}
                className={`group flex items-center gap-3 px-6 py-3.5 
                  bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                  hover:from-purple-600/20 hover:to-purple-500/20
                  text-purple-400 rounded-xl transition-all duration-300
                  border border-purple-500/20 hover:border-purple-500/40
                  ${isRefreshing ? 'opacity-50 cursor-not-allowed' : ''}`}
                disabled={isRefreshing}
              >
                <RefreshCw className={`w-5 h-5 transition-transform group-hover:rotate-180 
                  duration-700 ${isRefreshing ? 'animate-spin' : ''}`} />
                <span className="text-base font-medium">Refresh Dashboard</span>
              </button>
              <button
                onClick={() => navigate('/')}
                className="group flex items-center gap-3 px-6 py-3.5 
                  bg-gradient-to-r from-red-600/10 to-red-500/10 
                  hover:from-red-600/20 hover:to-red-500/20
                  text-red-400 rounded-xl transition-all duration-300
                  border border-red-500/20 hover:border-red-500/40"
              >
                <LogOut className="w-5 h-5" />
                <span className="text-base font-medium">Logout</span>
              </button>
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-6 mb-8
          border border-purple-500/20 shadow-lg shadow-purple-500/5">
          <div className="flex flex-wrap gap-4">
            {[
              { id: 'rooms', icon: Building, label: 'Rooms' },
              { id: 'roommates', icon: Users, label: 'Roommates' },
              { id: 'payments', icon: CreditCard, label: 'Payments' },
              { id: 'vacateRequests', icon: FileText, 
                label: `Vacate Requests ${vacateRequests.length > 0 ? 
                  `(${vacateRequests.length})` : ''}` },
              { id: 'grievances', icon: MessageSquare, 
                label: `Grievances ${grievances.length > 0 ? 
                  `(${grievances.length})` : ''}` }
            ].map(({ id, icon: Icon, label }) => (
              <button
                key={id}
                type="button"
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  handleTabChange(id);
                }}
                className={`flex items-center gap-3 px-6 py-4 rounded-xl text-base md:text-lg
                  transition-all duration-300 font-medium
                  ${activeTab === id 
                    ? 'bg-gradient-to-r from-purple-600 to-purple-500 text-white shadow-lg' 
                    : 'bg-[#2f2f2f]/50 text-purple-300 hover:bg-purple-600/20 border border-purple-500/20'}`}
              >
                <Icon className={`w-5 h-5 ${activeTab === id ? 'animate-pulse' : ''}`} />
                <span>{label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Main Content Area */}
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
          border border-purple-500/20 shadow-lg shadow-purple-500/5">
          {tabLoading ? (
            <div className="flex items-center justify-center py-12">
              <div className="w-12 h-12 border-4 border-purple-500/20 border-t-purple-500 
                rounded-full animate-spin"></div>
            </div>
          ) : (
            <>
              {activeTab === 'rooms' && (
                <div className="space-y-8">
                  <div className="flex justify-between items-center">
                    <h2 className="text-lg md:text-2xl font-bold bg-gradient-to-r from-purple-400 
                      to-purple-600 bg-clip-text text-transparent">
                      Room Management
                    </h2>
                    <button
                      onClick={() => setShowAddRoomModal(true)}
                      className="flex items-center gap-2 px-6 py-3 
                        bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                        hover:from-purple-600/20 hover:to-purple-500/20
                        text-purple-400 rounded-xl transition-all duration-300
                        border border-purple-500/20 hover:border-purple-500/40"
                    >
                      <Plus className="w-5 h-5" />
                      <span>Add Room</span>
                    </button>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {rooms.map((room) => (
                      <div
                        key={room.roomId}
                        onClick={(e) => handleRoomClick(room, e)}
                        className="group bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                          hover:bg-purple-600/10 transition-all duration-300 cursor-pointer
                          border border-purple-500/20 hover:border-purple-500/40
                          transform hover:-translate-y-1 hover:shadow-xl hover:shadow-purple-500/10">
                        <div className="flex items-center gap-3 mb-4">
                          <div className="p-2 rounded-lg bg-purple-500/10 group-hover:bg-purple-500/20
                            transition-all duration-300">
                            <Building className="w-6 h-6 text-purple-400" />
                          </div>
                          <h3 className="text-xl font-semibold text-purple-300">
                            Room {room.roomNumber}
                          </h3>
                        </div>
                        <div className="space-y-3">
                          {[
                            { label: 'Floor', value: room.floorNumber },
                            { label: 'Type', value: room.roomType },
                            { label: 'Capacity', value: `${room.currentCapacity}/${room.capacity}` },
                            { label: 'Price', value: `₹${room.price}/month` },
                            { label: 'AC', value: room.isAcAvailable ? 'Available' : 'Not Available' }
                          ].map(({ label, value }) => (
                            <div key={label} className="flex items-center gap-3 text-purple-200/80">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              <span className="font-medium">{label}:</span>
                              <span className="ml-auto">{value}</span>
                            </div>
                          ))}
                        </div>
                        <div className="flex justify-end gap-2 mt-4">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setSelectedRoomId(room.roomId);
                              setEditRoomData({
                                floorNumber: room.floorNumber,
                                roomType: room.roomType,
                                capacity: room.capacity,
                                currentCapacity: room.currentCapacity,
                                isAcAvailable: room.isAcAvailable,
                                price: room.price
                              });
                              setShowEditRoomModal(true);
                            }}
                            className="px-3 py-1.5 bg-blue-600/20 text-blue-400 
                              rounded-lg hover:bg-blue-600/30 transition-all duration-200
                              border border-blue-500/20 hover:border-blue-500/40"
                          >
                            Edit
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteRoom(room.roomId);
                            }}
                            className="px-3 py-1.5 bg-red-600/20 text-red-400 
                              rounded-lg hover:bg-red-600/30 transition-all duration-200
                              border border-red-500/20 hover:border-red-500/40"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {activeTab === 'roommates' && (
                <div className="space-y-6">
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                    <h2 className="text-2xl font-bold text-transparent bg-clip-text 
                      bg-gradient-to-r from-purple-400 to-purple-600">
                      Roommate Management
                    </h2>

                    <div className="flex flex-wrap items-center gap-4">
                      {/* Sorting Controls */}
                      <select
                        value={`${sortField}-${sortOrder}`}
                        onChange={(e) => {
                          const [field, order] = e.target.value.split('-');
                          handleSortChange(field);
                        }}
                        className="px-4 py-2 bg-[#242424]/80 backdrop-blur-lg text-purple-300 rounded-xl 
                          border border-purple-500/20 hover:border-purple-500/40 focus:outline-none
                          focus:border-purple-500/40 transition-all duration-200"
                      >
                        <option value="username-asc">Name (A-Z)</option>
                        <option value="username-desc">Name (Z-A)</option>
                        <option value="email-asc">Email (A-Z)</option>
                        <option value="email-desc">Email (Z-A)</option>
                        <option value="checkInDate-asc">Check-in (Oldest)</option>
                        <option value="checkInDate-desc">Check-in (Newest)</option>
                      </select>

                      {/* Rent Status Filter */}
                      <select
                        value={filterRentStatus}
                        onChange={(e) => handleFilterChange(e.target.value)}
                        className="px-4 py-2 bg-[#242424]/80 backdrop-blur-lg text-purple-300 rounded-xl 
                          border border-purple-500/20 hover:border-purple-500/40 focus:outline-none
                          focus:border-purple-500/40 transition-all duration-200"
                      >
                        <option value="">All Rent Status</option>
                        <option value="PAYMENT_DONE">Paid</option>
                        <option value="PAYMENT_PENDING">Pending</option>
                      </select>

                      <button
                        onClick={handleSendEmail}
                        disabled={isSendingNotification}
                        className={`flex items-center justify-center gap-2 px-6 py-2 
                          bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                          hover:from-purple-600/20 hover:to-purple-500/20 text-purple-400 
                          rounded-xl transition-all duration-300 border border-purple-500/20 
                          hover:border-purple-500/40 min-w-[160px]
                          ${isSendingNotification ? 'opacity-75 cursor-not-allowed' : ''}`}
                      >
                        {isSendingNotification ? (
                          <div className="flex items-center gap-2">
                            <svg className="animate-spin h-5 w-5 text-purple-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            <span>Sending...</span>
                          </div>
                        ) : (
                          <>
                            <Mail className="w-5 h-5" />
                            <span>Send Notification</span>
                          </>
                        )}
                      </button>
                    </div>
                  </div>

                  {/* Roommates Grid with Loading State */}
                  {roommatesLoading ? (
                    <div className="flex justify-center items-center h-48">
                      <div className="w-12 h-12 border-4 border-purple-500/20 border-t-purple-500 
                        rounded-full animate-spin"></div>
                    </div>
                  ) : roommates.length === 0 ? (
                    <div className="text-center py-12 text-purple-300/60">
                      <p className="text-lg">No roommates found</p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {roommates.map((roommate) => (
                        <div key={roommate.roommateId} className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                          border border-purple-500/20 transform hover:scale-[1.02] transition-all duration-300">
                          <h3 className="text-xl font-semibold text-purple-300 mb-4">{roommate.username}</h3>
                          <div className="space-y-3 text-purple-200/80">
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Email: {roommate.email}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Room: {roommate.roomNumber}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Rent: ₹{roommate.rentAmount}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Status: {roommate.rentStatus}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              {roommate.withFood ? 'Food Included' : 'Food Not Included'}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              CheckInDate: {roommate.checkInDate}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              CheckOutDate: {roommate.checkOutDate}
                            </p>
                          </div>
                          <button
                            onClick={() => handleVacateClick(roommate)}
                            className="mt-6 w-full px-6 py-3 bg-red-600/20 text-red-400 
                              rounded-xl hover:bg-red-600/30 transition-all duration-200
                              border border-red-500/20 hover:border-red-500/40"
                          >
                            Vacate
                          </button>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Pagination */}
                  {!roommatesLoading && (
                    <div className="flex justify-center items-center gap-4 mt-8">
                      <button
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 0}
                        className="px-4 py-2 bg-[#242424]/80 text-purple-300 rounded-xl 
                          border border-purple-500/20 hover:bg-purple-600/20 
                          disabled:opacity-50 disabled:cursor-not-allowed
                          transition-all duration-200"
                      >
                        Previous
                      </button>
                      <span className="text-purple-300">
                        Page {currentPage + 1} of {totalPages}
                      </span>
                      <button
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage >= totalPages - 1}
                        className="px-4 py-2 bg-[#242424]/80 text-purple-300 rounded-xl 
                          border border-purple-500/20 hover:bg-purple-600/20 
                          disabled:opacity-50 disabled:cursor-not-allowed
                          transition-all duration-200"
                      >
                        Next
                      </button>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'payments' && (
                <div className="space-y-6">
                  <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                    <h2 className="text-lg md:text-2xl font-bold text-transparent bg-clip-text 
                      bg-gradient-to-r from-purple-400 to-purple-600">
                      Payment Tracking
                    </h2>
                    
                    <div className="flex flex-wrap items-center gap-4">
                      {/* Search Controls */}
                      <div className="flex items-center gap-2">
                        <input
                          type="text"
                          placeholder="Search by name/room number..."
                          value={searchUsername}
                          onChange={(e) => setSearchUsername(e.target.value)}
                          onKeyPress={(e) => {
                            if (e.key === 'Enter') {
                              handleSearchPayments(searchUsername);
                            }
                          }}
                          className="px-4 py-2 bg-[#242424]/80 backdrop-blur-lg text-purple-300 rounded-xl 
                            border border-purple-500/20 hover:border-purple-500/40 focus:outline-none
                            focus:border-purple-500/40 min-w-[200px]"
                        />
                        <button
                          onClick={() => handleSearchPayments(searchUsername)}
                          disabled={isSearching}
                          className="px-4 py-2 bg-purple-500/20 text-purple-300 rounded-xl 
                            border border-purple-500/20 hover:bg-purple-600/30 
                            disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {isSearching ? 'Searching...' : 'Search'}
                        </button>
                        {searchUsername && (
                          <button
                            onClick={() => {
                              setSearchUsername('');
                              fetchSortedPayments();
                            }}
                            className="px-4 py-2 bg-red-500/20 text-red-300 rounded-xl 
                              border border-red-500/20 hover:bg-red-600/30"
                          >
                            Clear
                          </button>
                        )}
                      </div>

                      {/* Sorting Controls */}
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handlePaymentSort('username')}
                          className={`px-4 py-2 rounded-xl border transition-all duration-200
                            ${paymentSortField === 'username' 
                              ? 'bg-purple-600/20 text-purple-300 border-purple-500/40' 
                              : 'bg-[#242424]/80 text-purple-400 border-purple-500/20 hover:bg-purple-600/10'}`}
                        >
                          Name {paymentSortField === 'username' && (
                            <span>{paymentSortOrder === 'asc' ? '↑' : '↓'}</span>
                          )}
                        </button>
                        <button
                          onClick={() => handlePaymentSort('paymentDate')}
                          className={`px-4 py-2 rounded-xl border transition-all duration-200
                            ${paymentSortField === 'paymentDate' 
                              ? 'bg-purple-600/20 text-purple-300 border-purple-500/40' 
                              : 'bg-[#242424]/80 text-purple-400 border-purple-500/20 hover:bg-purple-600/10'}`}
                        >
                          Date {paymentSortField === 'paymentDate' && (
                            <span>{paymentSortOrder === 'asc' ? '↑' : '↓'}</span>
                          )}
                        </button>
                      </div>

                      {/* Date Filter */}
                      <input
                        type="date"
                        value={selectedPaymentDate}
                        onChange={(e) => setSelectedPaymentDate(e.target.value)}
                        className="px-4 py-2 bg-[#242424]/80 backdrop-blur-lg text-purple-300 rounded-xl 
                          border border-purple-500/20 hover:border-purple-500/40 focus:outline-none"
                      />
                      
                      {selectedPaymentDate && (
                        <button
                          onClick={() => setSelectedPaymentDate('')}
                          className="px-4 py-2 bg-red-600/20 text-red-400 rounded-xl 
                            border border-red-500/20 hover:bg-red-600/30"
                        >
                          Clear Date
                        </button>
                      )}

                      {/* Send Pending Button */}
                      <button
                        onClick={handleSendRentPendingNotification}
                        disabled={isSendingNotification}
                        className={`flex items-center gap-2 px-4 py-2 
                          bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                          hover:from-purple-600/20 hover:to-purple-500/20
                          text-purple-400 rounded-xl transition-all duration-300
                          border border-purple-500/20 hover:border-purple-500/40
                          ${isSendingNotification ? 'opacity-75 cursor-not-allowed' : ''}`}
                      >
                        {isSendingNotification ? (
                          <>
                            <RefreshCw className="w-5 h-5 animate-spin" />
                            <span>Sending...</span>
                          </>
                        ) : (
                          <>
                            <Mail className="w-5 h-5" />
                            <span>Send Remainder</span>
                          </>
                        )}
                      </button>
                    </div>
                  </div>

                  {/* Show "No results found" message when search yields no results */}
                  {!paymentsLoading && payments.length === 0 && searchUsername && (
                    <div className="text-center py-12 text-purple-300/60">
                      <p className="text-lg">No payments found for "{searchUsername}"</p>
                    </div>
                  )}

                  {/* Payments Grid with Loading State */}
                  <div className="relative min-h-[400px]">
                    {paymentsLoading ? (
                      <div className="absolute inset-0 flex items-center justify-center bg-[#242424]/50 backdrop-blur-sm
                        rounded-xl transition-opacity duration-300">
                        <div className="flex flex-col items-center gap-3">
                          <div className="w-10 h-10 border-4 border-purple-500/20 border-t-purple-500 
                            rounded-full animate-spin"></div>
                          <span className="text-purple-300/60">Loading payments...</span>
                        </div>
                      </div>
                    ) : null}
                    
                    <div className={`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6
                      transition-opacity duration-300 ${paymentsLoading ? 'opacity-50' : 'opacity-100'}`}>
                      {payments.map((payment) => (
                        <div key={payment.id} 
                          className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                            border border-purple-500/20 transform transition-all duration-300
                            hover:border-purple-500/40 hover:-translate-y-1">
                          <div className="space-y-3">
                            <div className="flex items-center justify-between">
                              <h3 className="text-xl font-semibold text-purple-300">
                                {payment.username}
                              </h3>
                              <span className={`px-3 py-1 rounded-full text-sm ${
                                payment.paymentStatus === 'PAYMENT_DONE'
                                  ? 'bg-green-500/20 text-green-400'
                                  : 'bg-purple-500/20 text-purple-400'
                              }`}>
                                {payment.paymentStatus}
                              </span>
                            </div>
                            <div className="space-y-2">
                              <p className="flex items-center gap-2 text-purple-200/80">
                                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                                Amount: ₹{payment.amount}
                              </p>
                              <p className="flex items-center gap-2 text-purple-200/80">
                                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                                Payment Date: {new Date(payment.paymentDate).toLocaleDateString()}
                              </p>
                              <p className="flex items-center gap-2 text-purple-200/80">
                                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                                Payment Method: {payment.paymentMethod}
                              </p>
                              <p className="flex items-center gap-2 text-purple-200/80">
                                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                                Room Number: {payment.roomNumber}
                              </p>
                              <p className="flex items-center gap-2 text-purple-200/80">
                                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                                Transaction ID: {payment.transactionId}
                              </p>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Pagination with Loading State */}
                  {paymentTotalPages > 1 && (
                    <div className="flex justify-center items-center gap-4 mt-8">
                      <button
                        onClick={() => handlePaymentPageChange(paymentCurrentPage - 1)}
                        disabled={paymentCurrentPage === 0 || paymentsLoading}
                        className={`px-4 py-2 bg-[#242424]/80 text-purple-300 rounded-xl 
                          border border-purple-500/20 transition-all duration-300
                          ${paymentsLoading 
                            ? 'opacity-50 cursor-not-allowed' 
                            : 'hover:bg-purple-600/20 hover:border-purple-500/40'}`}
                      >
                        Previous
                      </button>
                      <div className="flex items-center gap-2">
                        <span className={`text-purple-300 transition-opacity duration-300
                          ${paymentsLoading ? 'opacity-50' : 'opacity-100'}`}>
                          Page {paymentCurrentPage + 1} of {paymentTotalPages}
                        </span>
                        {paymentsLoading && (
                          <div className="w-4 h-4 border-2 border-purple-500/20 border-t-purple-500 
                            rounded-full animate-spin"></div>
                        )}
                      </div>
                      <button
                        onClick={() => handlePaymentPageChange(paymentCurrentPage + 1)}
                        disabled={paymentCurrentPage >= paymentTotalPages - 1 || paymentsLoading}
                        className={`px-4 py-2 bg-[#242424]/80 text-purple-300 rounded-xl 
                          border border-purple-500/20 transition-all duration-300
                          ${paymentsLoading 
                            ? 'opacity-50 cursor-not-allowed' 
                            : 'hover:bg-purple-600/20 hover:border-purple-500/40'}`}
                      >
                        Next
                      </button>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'vacateRequests' && (
                <div className="space-y-4">
                  <h2 className="text-lg md:text-2xl font-bold text-transparent bg-clip-text 
                    bg-gradient-to-r from-purple-400 to-purple-600 mb-4">
                    Pending Vacate Requests
                  </h2>
                  {vacateRequests.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {vacateRequests.map((request) => (
                        <div key={request.vacateRequestId} 
                          className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                          border border-purple-500/20 transform hover:scale-[1.02]
                          transition-all duration-300">
                          <h3 className="text-xl font-semibold text-purple-300 mb-4">{request.roommateName}</h3>
                          <div className="space-y-3 text-purple-200/80">
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Room: {request.roomNumber}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Reason: {request.vacateReason}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              CheckOut Date: {request.checkOutDate}
                            </p>
                          </div>
                          <button
                            onClick={() => handleMarkVacateRequestAsRead(request.vacateRequestId)}
                            className="mt-6 w-full px-6 py-3 bg-purple-600/20 text-purple-400 
                              rounded-xl hover:bg-purple-600/30 transition-all duration-200
                              border border-purple-500/20 hover:border-purple-500/40"
                          >
                            Mark as Read
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-center py-8 text-gray-500 dark:text-gray-400">
                      No pending vacate requests
                    </p>
                  )}
                </div>
              )}

              {activeTab === 'grievances' && (
                <div className="space-y-4">
                  <h2 className="text-lg md:text-xl font-bold text-transparent bg-clip-text 
                    bg-gradient-to-r from-purple-400 to-purple-600 mb-4">
                    Pending Grievances
                  </h2>
                  {grievances.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {grievances.map((grievance) => (
                        <div key={grievance.grievanceId} 
                          className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                          border border-purple-500/20 transform hover:scale-[1.02]
                          transition-all duration-300">
                          <h3 className="text-xl font-semibold text-purple-300 mb-4">{grievance.roommateName}</h3>
                          <div className="space-y-3 text-purple-200/80">
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Room: {grievance.roomNumber}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Description: {grievance.grievanceContent}
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Issue From: {grievance.createdAt}
                            </p>
                          </div>
                          <button
                            onClick={() => handleMarkGrievanceAsRead(grievance.grievanceId)}
                            className="mt-6 w-full px-6 py-3 bg-purple-600/20 text-purple-400 
                              rounded-xl hover:bg-purple-600/30 transition-all duration-200
                              border border-purple-500/20 hover:border-purple-500/40"
                          >
                            Resolved
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-center py-8 text-gray-500 dark:text-gray-400">
                      No pending grievances
                    </p>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Modals with updated styling */}
      {selectedRoom && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-2xl w-full border border-purple-500/20 shadow-2xl
            transform transition-all duration-300">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-bold text-transparent bg-clip-text 
                bg-gradient-to-r from-purple-400 to-purple-600">
                Room {selectedRoom.roomNumber} Details
              </h2>
              <button
                onClick={handleCloseRoomDetails}
                className="text-purple-400 hover:text-purple-300 transition-colors"
              >
                <X className="w-6 h-6" />
              </button>
            </div>
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                {[
                  { label: 'Floor Number', value: selectedRoom.floorNumber },
                  { label: 'Room Type', value: selectedRoom.roomType },
                  { label: 'Capacity', value: `${selectedRoom.currentCapacity}/${selectedRoom.capacity}` },
                  { label: 'Price', value: `${selectedRoom.price}/month` },
                  { label: 'AC', value: selectedRoom.isAcAvailable ? 'Available' : 'Not Available' }
                ].map(({ label, value }) => (
                  <div key={label} className="flex items-center gap-3 text-purple-200/80">
                    <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                    <span className="font-medium">{label}:</span>
                    <span className="ml-auto">{value}</span>
                  </div>
                ))}
              </div>
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-transparent bg-clip-text 
                  bg-gradient-to-r from-purple-400 to-purple-600">
                  Roommates
                </h3>
                {selectedRoom.roommateDTO && selectedRoom.roommateDTO.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {selectedRoom.roommateDTO.map((roommate) => (
                      <div key={roommate.roommateId} 
                        className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                        border border-purple-500/20 transform hover:scale-[1.02]
                        transition-all duration-300">
                        <h4 className="text-lg font-semibold text-purple-300 mb-4">{roommate.username}</h4>
                        <div className="space-y-3 text-purple-200/80">
                          <p className="flex items-center gap-2">
                            <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                            Email: {roommate.email}
                          </p>
                          <p className="flex items-center gap-2">
                            <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                            Rent: {'\u20B9'}{roommate.rentAmount}
                          </p>
                          <p className="flex items-center gap-2">
                            <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                            Status: {roommate.rentStatus}
                          </p>
                          <p className="flex items-center gap-2">
                            <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                            {roommate.withFood ? 'Food Included' : 'Food Not Included'}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-center py-8 text-purple-200/80">
                    No roommates in this room
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {showVacateModal && selectedRoommate && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl
            transform transition-all duration-300">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Confirm Vacate
            </h2>
            <div className="space-y-3 text-purple-200/80">
              <p className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                Roommate: {selectedRoommate.username}
              </p>
              <p className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                Room: {selectedRoommate.roomNumber}
              </p>
            </div>
            <p className="mt-6 text-purple-300">
              Are you sure you want to proceed with this vacate ?
            </p>
            <div className="flex justify-end space-x-4 mt-8">
              <button
                onClick={() => setShowVacateModal(false)}
                className="px-6 py-3 bg-[#2f2f2f]/50 text-purple-300 
                  rounded-xl hover:bg-purple-600/20 transition-all duration-200
                  border border-purple-500/20 hover:border-purple-500/40"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmVacate}
                className="px-6 py-3 bg-red-600/20 text-red-400 
                  rounded-xl hover:bg-red-600/30 transition-all duration-200
                  border border-red-500/20 hover:border-red-500/40"
              >
                Confirm Vacate
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Room Modal */}
      {showAddRoomModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Add New Room
            </h2>
            <form onSubmit={handleAddRoom} className="space-y-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Floor Number</label>
                <input
                  type="number"
                  placeholder="Enter floor number"
                  value={newRoomData.floorNumber}
                  onChange={(e) => setNewRoomData({...newRoomData, floorNumber: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  required
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Room Number</label>
                <input
                  type="text"
                  placeholder="Enter room number"
                  value={newRoomData.roomNumber}
                  onChange={(e) => setNewRoomData({...newRoomData, roomNumber: e.target.value})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40"
                  required
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Room Type</label>
                <input
                  type="text"
                  placeholder="Enter room type (e.g., Single Sharing, Double Sharing)"
                  value={newRoomData.roomType}
                  onChange={(e) => setNewRoomData({...newRoomData, roomType: e.target.value})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40"
                  required
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Total Capacity</label>
                <input
                  type="number"
                  placeholder="Enter total capacity"
                  value={newRoomData.capacity}
                  onChange={(e) => setNewRoomData({...newRoomData, capacity: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  required
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Current Capacity</label>
                <input
                  type="number"
                  placeholder="Enter current capacity"
                  value={newRoomData.currentCapacity}
                  onChange={(e) => setNewRoomData({...newRoomData, currentCapacity: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  required
                />
              </div>

              <div className="flex items-center gap-3 p-3 rounded-xl bg-[#2f2f2f]/50 
                border border-purple-500/20">
                <input
                  type="checkbox"
                  checked={newRoomData.isAcAvailable}
                  onChange={(e) => setNewRoomData({...newRoomData, isAcAvailable: e.target.checked})}
                  className="w-5 h-5 rounded border-purple-500/20"
                />
                <label className="text-purple-200">AC Available</label>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Price (per month)</label>
                <input
                  type="number"
                  placeholder="Enter room price"
                  value={newRoomData.price}
                  onChange={(e) => setNewRoomData({...newRoomData, price: parseFloat(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                  required
                />
              </div>

              <div className="flex gap-4 mt-6">
                <button
                  type="submit"
                  className="flex-1 bg-purple-600 text-white py-3 rounded-xl 
                    hover:bg-purple-500 transition-all duration-300"
                >
                  Add Room
                </button>
                <button
                  type="button"
                  onClick={() => setShowAddRoomModal(false)}
                  className="flex-1 bg-[#2f2f2f]/50 text-purple-300 py-3 rounded-xl 
                    hover:bg-[#2f2f2f]/70 transition-all duration-300
                    border border-purple-500/20 hover:border-purple-500/40"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Room Modal */}
      {showEditRoomModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Edit Room
            </h2>
            <form onSubmit={handleEditRoom} className="space-y-4">
              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Floor Number</label>
                <input
                  type="number"
                  placeholder="Enter floor number"
                  value={editRoomData.floorNumber ?? ''}
                  onChange={(e) => setEditRoomData({...editRoomData, floorNumber: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Room Type</label>
                <input
                  type="text"
                  placeholder="Enter room type"
                  value={editRoomData.roomType ?? ''}
                  onChange={(e) => setEditRoomData({...editRoomData, roomType: e.target.value})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Total Capacity</label>
                <input
                  type="number"
                  placeholder="Enter total capacity"
                  value={editRoomData.capacity ?? ''}
                  onChange={(e) => setEditRoomData({...editRoomData, capacity: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Current Capacity</label>
                <input
                  type="number"
                  placeholder="Enter current capacity"
                  value={editRoomData.currentCapacity ?? ''}
                  onChange={(e) => setEditRoomData({...editRoomData, currentCapacity: parseInt(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
              </div>

              <div className="flex items-center gap-3 p-3 rounded-xl bg-[#2f2f2f]/50 
                border border-purple-500/20">
                <input
                  type="checkbox"
                  checked={editRoomData.isAcAvailable ?? false}
                  onChange={(e) => setEditRoomData({...editRoomData, isAcAvailable: e.target.checked})}
                  className="w-5 h-5 rounded border-purple-500/20"
                />
                <label className="text-purple-200">AC Available</label>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-purple-300">Price (per month)</label>
                <input
                  type="number"
                  placeholder="Enter price"
                  value={editRoomData.price ?? ''}
                  onChange={(e) => setEditRoomData({...editRoomData, price: parseFloat(e.target.value)})}
                  className="w-full p-3 rounded-xl bg-[#2f2f2f]/50 text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                />
              </div>

              <div className="flex gap-4 mt-6">
                <button
                  type="submit"
                  className="flex-1 bg-purple-600 text-white py-3 rounded-xl 
                    hover:bg-purple-500 transition-all duration-300"
                >
                  Save Changes
                </button>
                <button
                  type="button"
                  onClick={() => setShowEditRoomModal(false)}
                  className="flex-1 bg-[#2f2f2f]/50 text-purple-300 py-3 rounded-xl 
                    hover:bg-[#2f2f2f]/70 transition-all duration-300
                    border border-purple-500/20 hover:border-purple-500/40"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteConfirmation && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-purple-300 mb-4">
              Confirm Deletion
            </h2>
            <p className="text-purple-200/80 mb-6">
              Are you sure you want to delete this room? This action cannot be undone.
            </p>
            <div className="flex gap-4">
              <button
                onClick={handleConfirmDelete}
                className="flex-1 bg-red-600/20 text-red-400 py-3 rounded-xl 
                  hover:bg-red-600/30 transition-all duration-300
                  border border-red-500/20 hover:border-red-500/40"
              >
                Delete Room
              </button>
              <button
                onClick={() => setShowDeleteConfirmation(false)}
                className="flex-1 bg-[#2f2f2f]/50 text-purple-300 py-3 rounded-xl 
                  hover:bg-[#2f2f2f]/70 transition-all duration-300
                  border border-purple-500/20 hover:border-purple-500/40"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default React.memo(OwnerDashboard);