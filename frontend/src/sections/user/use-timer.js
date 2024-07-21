import { useEffect, useMemo, useState } from 'react';
import { calculateTimeDifference, getTimeString } from '../../helpers/helper-time-functions';


export const UseTimer = ({ closesAt }) => {
  const closesAtDate = useMemo(() => new Date(closesAt), [closesAt]);
  const [totalSecondsLeft, setTotalSecondsLeft] = useState(calculateTimeDifference(closesAt));
  useEffect(() => {
    const timer = setInterval(() => {
      const timeDifferenceInSeconds = calculateTimeDifference(closesAtDate);
      if (timeDifferenceInSeconds >= 0) {
        setTotalSecondsLeft(timeDifferenceInSeconds);
      } else {
        clearInterval(timer);
      }
    }, 1000);
    return () => {
      clearInterval(timer);
    };
  }, [closesAtDate]);
  return {
    totalSecondsLeft: totalSecondsLeft ? totalSecondsLeft : calculateTimeDifference(closesAtDate),
    timeString: totalSecondsLeft ? getTimeString(totalSecondsLeft): getTimeString(calculateTimeDifference(closesAtDate))
  };
};