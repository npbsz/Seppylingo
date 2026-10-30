import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BubbleBobbleComponent } from './bubble-bobble';

describe('BubbleBobble', () => {
  let component: BubbleBobbleComponent;
  let fixture: ComponentFixture<BubbleBobbleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BubbleBobbleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BubbleBobbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
