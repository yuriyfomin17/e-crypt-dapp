import Dialog from '@mui/material/Dialog';
import {
  Box,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Divider
} from '@mui/material';
import PropTypes from 'prop-types';
import { useState } from 'react';
import {
  addUserBet,
  createAdminBet
} from '../../helpers/helper-rest-functions';
import { GameButton } from '../../components/game-button';
import { GameInputField } from '../../components/game-input-field';
import { ROLES } from '../../helpers/helper-constants';
import { getUserId, getUserRole } from '../../helpers/helper-functions';

export const UserGameForm = ({ setIsNewGameFormOpened, isNewGameFormOpened }) => {
  const role = getUserRole()

  const [isLoading, setIsLoading] = useState(false);
  const [betId, setBetId] = useState('');
  const gameButtonTitle = role === ROLES.ROLE_USER ? 'Save' : 'Create Game';
  const cardHeaderTitle = role === ROLES.ROLE_USER
    ? 'Add new game by ID'
    : 'Create New Game';

  const [durationInMinutes, setDurationInMinutes] = useState('');
  const [title, setTitle] = useState('');
  const [ethTicketPrice, setEthTicketPrice] = useState('0.00');
  const handleClose = () => {
    setIsNewGameFormOpened(false);
  };
  const addUserGame = async () => {
    setIsLoading(true);
    await addUserBet(betId);
    setIsNewGameFormOpened(false);
    setIsLoading(false);
  };

  const createNewAdminGame = async () => {
    const newBetPayload = {
      adminUserId: getUserId(),
      title,
      durationInMinutes,
      ethTicketPrice
    };

    await createAdminBet(newBetPayload);
    setIsNewGameFormOpened(false);
  };
  return (
    <>
      <Dialog open={isNewGameFormOpened}
              onClose={handleClose}
              fullWidth
      >
        <form
          autoComplete="off"
          noValidate

        >
          <Card>
            <CardHeader
              title={cardHeaderTitle}
            />
            <CardContent sx={{ pt: 0 }}>
              <Box sx={{ m: -1.5 }}>
                <GameInputField
                  title={title}
                  setDurationInMinutes={setDurationInMinutes}
                  durationInMinutes={durationInMinutes}
                  ethTicketPrice={ethTicketPrice}
                  betId={betId}
                  setBetId={setBetId}
                  setTitle={setTitle}
                  setEthTicketPrice={setEthTicketPrice}
                />
              </Box>
            </CardContent>
            <Divider/>
            <CardActions sx={{ justifyContent: 'flex-end' }}>
              <GameButton
                isLoading={isLoading}
                addGame={addUserGame}
                submitNewAdminBet={createNewAdminGame}
                gameButtonTitle={gameButtonTitle}
              />
            </CardActions>
          </Card>
        </form>
      </Dialog>
    </>
  );
};

UserGameForm.propTypes = {
  setIsNewGameFormOpened: PropTypes.func,
  isNewGameFormOpened: PropTypes.bool
};