import React from 'react';
import { Star } from 'lucide-react';

interface RatingStarsProps {
  rating: number;
  count?: number;
  showCount?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export const RatingStars: React.FC<RatingStarsProps> = ({
  rating,
  count,
  showCount = true,
  size = 'sm',
}) => {
  const iconSize = size === 'lg' ? 'w-5 h-5' : size === 'md' ? 'w-4 h-4' : 'w-3.5 h-3.5';

  return (
    <div className="inline-flex items-center gap-1.5">
      <div className="flex items-center text-amber-400">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`${iconSize} ${
              rating >= star
                ? 'fill-amber-400 text-amber-400'
                : rating >= star - 0.5
                ? 'fill-amber-200 text-amber-400'
                : 'text-gray-300'
            }`}
          />
        ))}
      </div>
      <span className="text-xs font-semibold text-gray-700">{rating > 0 ? rating.toFixed(1) : 'New'}</span>
      {showCount && count !== undefined && count > 0 && (
        <span className="text-xs text-gray-400">({count})</span>
      )}
    </div>
  );
};
