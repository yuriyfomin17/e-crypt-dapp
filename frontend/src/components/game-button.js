import CircularProgress from '@mui/material/CircularProgress';
import Button from '@mui/material/Button';
import { ROLES } from '../helpers/helper-constants';
import { getUserRole } from '../helpers/helper-functions';

export const GameButton = ({ isLoading, addGame, submitNewAdminBet, gameButtonTitle }) => {
  if (isLoading) {
    return <CircularProgress/>;
  }

  if (ROLES.ROLE_USER === getUserRole()) {
    return <Button variant="contained"
                   disabled={isLoading}
                   onClick={addGame}>{gameButtonTitle}</Button>;
  }
  if (ROLES.ROLE_ADMIN === getUserRole()) {
    return <Button variant="contained"
                   disabled={isLoading}
                   onClick={submitNewAdminBet}>{gameButtonTitle}</Button>;
  }
  return <></>;
};