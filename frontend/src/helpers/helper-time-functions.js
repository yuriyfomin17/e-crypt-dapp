export const calculateTimeDifference = (closesAtSeconds) => {
  const currentTime = new Date(Date.UTC(
    new Date().getUTCFullYear(),
    new Date().getUTCMonth(),
    new Date().getUTCDate(),
    new Date().getUTCHours(),
    new Date().getUTCMinutes(),
    new Date().getUTCSeconds(),
    new Date().getUTCMilliseconds()
  ));

  return Math.round((closesAtSeconds - currentTime) / 1000);
};

export const getTimeString = (timeDifference) => {
  const minutes = Math.floor(timeDifference / 60);
  const seconds = timeDifference - 60 * minutes;
  if (minutes < 10 && seconds < 10) {
    return '0' + minutes + ':' + '0' + seconds;
  } else if (minutes < 10 && seconds >= 10) {
    return '0' + minutes + ':' + seconds;
  } else if (minutes >= 10 && seconds < 10) {
    return minutes + ':' + '0'+ seconds;
  }
  return minutes + ':' + seconds;
};