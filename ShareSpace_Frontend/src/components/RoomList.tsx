import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Room } from '../types';
import { api } from '../api';
import { Bed, AirVent, UtensilsCrossed, Users } from 'lucide-react';

export default function RoomList() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const response = await api.getAllRooms();
        setRooms(response.data);
      } catch (error) {
        console.error('Error fetching rooms:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchRooms();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8 text-center">Available Rooms</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {rooms.map((room) => (
          <div
            key={room.roomId}
            className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
          >
            <div className="p-6">
              <h2 className="text-xl font-semibold mb-4">{room.roomType}</h2>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <AirVent className="w-5 h-5 text-gray-600" />
                  <span>{room.isAcAvailable ? 'AC' : 'Non-AC'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <UtensilsCrossed className="w-5 h-5 text-gray-600" />
                  <span>{room.withFood ? 'Food Included' : 'Food Not Included'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="w-5 h-5 text-gray-600" />
                  <span>Capacity: {room.capacity}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Bed className="w-5 h-5 text-gray-600" />
                  <span>Floor Number: {room.floorNumber}</span>
                </div>
              </div>
              <div className="mt-6">
                <button
                  onClick={() => navigate(`/room/${room.roomId}`)}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors duration-300"
                >
                  View Details
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}