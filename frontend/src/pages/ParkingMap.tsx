import React from 'react';
import { ParkingSpot } from '../types/parking';

interface ParkingMapProps {
    spots: ParkingSpot[];
    onSpotSelect: (spot: ParkingSpot) => void;
    selectedSpotId?: number;
}

export const ParkingMap: React.FC<ParkingMapProps> = ({ spots, onSpotSelect, selectedSpotId }) => {
    // Group spots by row
    const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
    const groupedSpots = rows.reduce((acc, row) => {
        acc[row] = Array.from({ length: 10 }, (_, i) => {
            const code = `${row}${(i + 1).toString().padStart(2, '0')}`;
            return spots.find(s => s.code === code);
        });
        return acc;
    }, {} as Record<string, (ParkingSpot | undefined)[]>);

    return (
        <div className="parking-map">
            <div className="parking-legend">
                <span className="legend-item"><div className="spot-box available"></div> Disponible</span>
                <span className="legend-item"><div className="spot-box occupied"></div> Occupé / Non trouvé</span>
                <span className="legend-item"><div className="spot-box selected"></div> Sélectionné</span>
                <span className="legend-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path></svg>
                    Chargeur électrique (Rangées A et F)
                </span>
            </div>

            <div className="parking-grid">
                {rows.map(row => (
                    <div key={row} className="parking-row">
                        <div className="row-label">{row}</div>
                        <div className="spot-slots">
                            {groupedSpots[row].map((spot, index) => {
                                const isAvailable = !!spot;
                                const isSelected = spot?.id === selectedSpotId;
                                const isElectric = row === 'A' || row === 'F';

                                let spotClass = 'spot-box';
                                if (!isAvailable) spotClass += ' occupied';
                                else if (isSelected) spotClass += ' selected';
                                else spotClass += ' available';

                                return (
                                    <div
                                        key={`${row}-${index}`}
                                        className={spotClass}
                                        onClick={() => {
                                            if (spot) onSpotSelect(spot);
                                        }}
                                        title={isAvailable ? `Place ${spot.code}` : `Non disponible`}
                                    >
                                        <span className="spot-code">{row}{index + 1}</span>
                                        {isElectric && (
                                            <span className="electric-icon" title="Prise électrique">
                                                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path></svg>
                                            </span>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};
