import {
  Card,
  CardHeader

} from '@mui/material';

import PropTypes from 'prop-types';
import { useState } from 'react';
import { BettingForm } from './betting-form';
import { BetCardContent } from '../../components/bet-card-content';
import { UserBetCardFooter } from '../../components/user-bet/user-bet-card-footer';
import { BET_STATUSES } from '../../helpers/helper-constants';

const getSubHeaderTitle = (currentBet) => {
  
  if (currentBet.status === BET_STATUSES.CREATED) {
    return 'Game is not started';
  }
  if (currentBet.status === BET_STATUSES.OPENED) {
    return 'Opened -> Predict result';
  }
  if (currentBet.status === BET_STATUSES.CLOSED) {
    return 'Submission is closed';
  }
  return 'Expired';
};

export const UserBetCard = ({ currentBet }) => {
  const [isBettingFormOpen, setIsBettingFormOpen] = useState(false);
  const [betOutcome, setBetOutcome] = useState(false);
  const subHeader = getSubHeaderTitle(currentBet);
  return (
    <Card
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        border: '1px solid',
        borderColor: 'neutral.400'
      }}
    >
      <BettingForm betId={currentBet.id}
                   ethTicketPrice={currentBet.ethTicketPrice}
                   isBettingFormOpen={isBettingFormOpen}
                   setIsBettingFormOpen={setIsBettingFormOpen}
                   betOutcome={betOutcome}
                   title={currentBet.title}/>
      <CardHeader sx={{ alignSelf: 'center', marginBottom: '5px' }}
                  titleTypographyProps={{ textAlign: 'center' }}
                  title={currentBet.title}
                  subheaderTypographyProps={{ textAlign: 'center' }}
                  subheader={'ID: ' + currentBet.id + ' | Status: ' + subHeader}
      />
      <BetCardContent currentBet={currentBet}/>
      <UserBetCardFooter
        currentBet={currentBet}
        setBetOutcome={setBetOutcome}
        setIsBettingFormOpen={setIsBettingFormOpen}
      />
    </Card>
  );
};

UserBetCard.propTypes = {
  currentBet: PropTypes.object.isRequired
};