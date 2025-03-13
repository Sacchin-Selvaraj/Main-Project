import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Room } from '../types';
import { api } from '../api';
import { AirVent, UtensilsCrossed, Users, Bed, IndianRupee } from 'lucide-react';
import toast from 'react-hot-toast';

export default function RoomDetail() {
  const { roomId } = useParams<{ roomId: string }>();
  const [room, setRoom] = useState<Room | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRoom = async () => {
      if (!roomId) return;
      try {
        const response = await api.getRoom(roomId);
        setRoom(response.data);
      } catch (error) {
        toast.error('Error fetching room details');
        navigate('/');
      } finally {
        setLoading(false);
      }
    };

    fetchRoom();
  }, [roomId, navigate]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-[#1a1a1a]">
        <div className="w-12 h-12 border-4 border-purple-500/20 border-t-purple-500 
          rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!room) {
    return (
      <div className="min-h-screen bg-[#1a1a1a] p-4 md:p-8">
        <div className="container mx-auto text-center">
          <h1 className="text-2xl md:text-3xl font-bold text-transparent bg-clip-text 
            bg-gradient-to-r from-purple-400 to-purple-600">
            Room not found
          </h1>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#1a1a1a] py-8 md:py-12">
      <div className="max-w-4xl mx-auto px-4 md:px-8">
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl 
          shadow-xl overflow-hidden border border-purple-500/20
          hover:border-purple-500/30 transition-all duration-300">
          <div className="p-6 md:p-8">
            <h1 className="text-2xl md:text-3xl font-bold mb-8 
              text-transparent bg-clip-text bg-gradient-to-r 
              from-purple-400 to-purple-600">
              {room.roomType} Room
            </h1>
            
            <div className="space-y-4 md:space-y-6">
              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <AirVent className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  {room.isAcAvailable ? 'AC Room' : 'Non-AC Room'}
                </span>
              </div>

              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <UtensilsCrossed className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  {room.withFood ? 'Food Service Available' : 'Food Service Optional'}
                </span>
              </div>

              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <Users className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  Capacity: {room.capacity} persons
                </span>
              </div>

              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <Bed className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  Current Capacity: {room.currentCapacity} persons
                </span>
              </div>
              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <IndianRupee className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  Food Included: {'\u20B9'}{room.price}/month 
                </span>
              </div>
              <div className="group flex items-center gap-3 p-4 rounded-xl
                bg-[#2f2f2f]/50 hover:bg-[#2f2f2f]/70
                border border-purple-500/20 hover:border-purple-500/40
                transition-all duration-300">
                <IndianRupee className="w-5 h-5 md:w-6 md:h-6 text-purple-400 
                  group-hover:scale-110 transition-transform duration-300" />
                <span className="text-base md:text-lg font-medium text-purple-300
                  group-hover:text-purple-200 transition-colors duration-300">
                  Food Not Included: {'\u20B9'}{room.price-1000}/month 
                </span>
              </div>

            </div>

            <div className="mt-8 space-x-4">
              <button
                onClick={() => navigate(`/book/${room.roomId}`)}
                className="bg-gradient-to-r from-purple-600 to-purple-500 
                  text-white py-3 px-8 rounded-xl font-semibold 
                  hover:from-purple-500 hover:to-purple-400
                  transform hover:scale-[1.02] active:scale-[0.98]
                  transition-all duration-300 shadow-lg 
                  hover:shadow-purple-500/25 text-base md:text-lg"
              >
                Book Now
              </button>
              <button
                onClick={() => navigate('/')}
                className="bg-[#2f2f2f]/50 text-purple-300 
                  py-3 px-8 rounded-xl font-semibold
                  hover:bg-[#2f2f2f]/70 hover:text-purple-200
                  transform hover:scale-[1.02] active:scale-[0.98]
                  transition-all duration-300 border border-purple-500/20 
                  hover:border-purple-500/40 text-base md:text-lg"
              >
                Back to Search
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}