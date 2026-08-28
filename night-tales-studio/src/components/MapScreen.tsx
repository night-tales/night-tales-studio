import { APIProvider, Map, AdvancedMarker } from '@vis.gl/react-google-maps';

const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '';

export default function MapScreen() {
  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950">
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm">
        <h2 className="text-zinc-100 font-bold text-center">الخريطة</h2>
      </div>
      
      <div className="flex-1 w-full h-full relative">
        {API_KEY ? (
          <APIProvider apiKey={API_KEY}>
            <Map
              defaultZoom={12}
              defaultCenter={{ lat: 24.7136, lng: 46.6753 }} // Riyadh default
              gestureHandling={'greedy'}
              disableDefaultUI={false}
              mapId="DEMO_MAP_ID"
              className="w-full h-full"
              internalUsageAttributionIds={["gmp_mcp_codeassist_v1_aistudio"]}
            >
              <AdvancedMarker position={{ lat: 24.7136, lng: 46.6753 }} />
            </Map>
          </APIProvider>
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-zinc-500 p-6 text-center gap-4">
            <div className="w-16 h-16 rounded-full bg-zinc-900 flex items-center justify-center">
              <span className="text-2xl">🗺️</span>
            </div>
            <div>
              <h3 className="font-semibold text-zinc-300 mb-1">مفتاح الخريطة مفقود</h3>
              <p className="text-sm">يرجى إضافة VITE_GOOGLE_MAPS_API_KEY في إعدادات البيئة</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
